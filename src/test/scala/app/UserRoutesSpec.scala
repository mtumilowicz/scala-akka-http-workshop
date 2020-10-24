package app

import java.util.concurrent.TimeUnit

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import app.domain.UserId
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import app.gateway.{UserHandler, UserRoutes, out}
import app.infrastructure.UserServiceConfiguration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, TimeUnit.SECONDS))

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val userRegistry = testKit.spawn(UserServiceConfiguration.inMemoryBehaviour)
  lazy val routes = new UserRoutes(new UserHandler(userRegistry)).userRoutes

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import app.infrastructure.JsonFormats._

  "UserRoutes" should {
    "return no users if no present" in {
//      when
      val request = HttpRequest(uri = "/users")

//      then
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[UsersApiOutput] should be (UsersApiOutput(Seq()))
      }
    }

    "return users" in {
//      given
      createUser()

//      when
      val request = HttpRequest(uri = "/users")

//      then
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[UsersApiOutput] should not be (UsersApiOutput(Seq()))
      }
    }

    "create user" in {
//      given
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue

//      when
      val request = Post("/users").withEntity(userEntity)

//      then
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val output = entityAs[UserApiOutput]
        output.id should not be (null)
        output.name should be ("Kapi")
        output.age should be (42)
      }
    }

    "get existing user by id" in {
//      given
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue
      val request = Post("/users").withEntity(userEntity)
      request ~> routes ~> check {
        val id = entityAs[UserApiOutput].id

//        when
        val get = Get(uri = "/users/" + id)

//        then
        get ~> routes ~> check {
          status should ===(StatusCodes.OK)
          val outputOfGet = entityAs[UserApiOutput]
          outputOfGet.id should be (id)
          outputOfGet.name should be ("Kapi")
          outputOfGet.age should be (42)
        }
      }
    }

    "remove existing user by id" in {
//      given
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        val id = entityAs[UserApiOutput].id

//        when
        val delete = Delete(uri = "/users/" + id)

//        then
        delete ~> routes ~> check {
          status should ===(StatusCodes.OK)
          entityAs[UserId].raw should be (id)
        }
      }
    }

    "update existing user" in {
//      given
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val id = entityAs[UserApiOutput].id

        val userPut = ReplaceUserApiInput(id, "Kapi2", 42, "jp")
        val userEntity = Marshal(userPut).to[MessageEntity].futureValue // futureValue is from ScalaFutures

//        when
        val requestPut = Put(uri = "/users/" + id).withEntity(userEntity)

//        then
        requestPut ~> routes ~> check {
          status should ===(StatusCodes.OK)

          val outputPut = entityAs[UserApiOutput]
          outputPut.id should be (id)
          outputPut.name should be ("Kapi2")
          outputPut.age should be (42)
        }
      }
    }
  }

  def createUser(): Unit = {
    val user = NewUserApiInput("Kapi", 42, "jp")
    val userEntity = Marshal(user).to[MessageEntity].futureValue

    val request = Post("/users").withEntity(userEntity)

    request ~> routes
  }
}