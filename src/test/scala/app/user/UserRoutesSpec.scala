package app.user

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import app.domain.user.UserId
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import app.gateway.{UserHandler, UserRoutes}
import app.infrastructure.JsonFormats._
import app.infrastructure.UserServiceConfiguration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, TimeUnit.SECONDS))
  val userService = testKit.spawn(UserServiceConfiguration.inMemoryBehaviour)
  lazy val routes = new UserRoutes(new UserHandler(userService)).userRoutes

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  "UserRoutes" should {
    "return no users if no present" in {
      //      when
      val request = HttpRequest(uri = "/users")

      //      then
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[UsersApiOutput] should be(UsersApiOutput(Seq()))
      }
    }

    "return users if they are present" in {
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
      val user = NewUserApiInput("Kapi", 42)
      val userEntity = Marshal(user).to[MessageEntity].futureValue

      //      when
      val request = Post("/users").withEntity(userEntity)

      //      then
      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        val output = entityAs[UserApiOutput]
        output.id should not be (null)
        output.name should be("Kapi")
        output.budget should be(42)

        header("location").map(_.value()) should ===(Some(s"http://localhost:8080/users/${output.id}"))
      }
    }

    "get existing user by id" in {
      //      given
      val id = createUser().id

      //        when
      val get = Get(uri = "/users/" + id)

      //        then
      get ~> routes ~> check {
        status should ===(StatusCodes.OK)
        val outputOfGet = entityAs[UserApiOutput]
        outputOfGet.id should be(id)
        outputOfGet.name should be("Kapi")
        outputOfGet.budget should be(42)
      }
    }

    "get not existing user by id" in {
      //        when
      val get = Get(uri = "/users/not-present")

      //        then
      get ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "remove existing user by id" in {
      //      given
      val id = createUser().id

      //        when
      val delete = Delete(uri = "/users/" + id)

      //        then
      delete ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[UserId].raw should be(id)
      }
    }

    "remove not existing user by id" in {
      //        when
      val delete = Delete(uri = "/users/not-present")

      //        then
      delete ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "update existing user" in {
      //      given
      val id = createUser().id
      val userPut = ReplaceUserApiInput("Kapi2", 123)
      val userEntity = Marshal(userPut).to[MessageEntity].futureValue

      //        when
      val requestPut = Put(uri = "/users/" + id).withEntity(userEntity)

      //        then
      requestPut ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPut = entityAs[UserApiOutput]
        outputPut.id should be(id)
        outputPut.name should be("Kapi2")
        outputPut.budget should be(123)
      }
    }

    "update not existing user" in {
      //      given
      val userPut = ReplaceUserApiInput("Kapi2", 123)
      val userEntity = Marshal(userPut).to[MessageEntity].futureValue

      //        when
      val requestPut = Put(uri = "/users/not-present").withEntity(userEntity)

      //        then
      requestPut ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    def createUser(): UserApiOutput = {
      val user = NewUserApiInput("Kapi", 42)
      val userEntity = Marshal(user).to[MessageEntity].futureValue

      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        entityAs[UserApiOutput]
      }
    }
  }
}