package app

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import app.domain.UserId
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.out.UserApiOutput
import app.gateway.{UserHandler, UserRoutes}
import app.infrastructure.UserServiceConfiguration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val userRegistry = testKit.spawn(UserServiceConfiguration.inMemoryBehaviour)
  lazy val routes = new UserRoutes(new UserHandler(userRegistry)).userRoutes

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import app.infrastructure.JsonFormats._

  "UserRoutes" should {
    "return no users if no present (GET /users)" in {
      val request = HttpRequest(uri = "/users")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"users":[]}""")
      }
    }

    "be able to add users (POST /users)" in {
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        def output = entityAs[UserApiOutput]
        output.id should not be (null)
        output.name == "Kapi"
        output.age == 42
      }
    }

    "be able to get user (GET /users)" in {
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        def output = entityAs[UserApiOutput]

        val requestG = Get(uri = "/users/" + output.id)

        requestG ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)


          def outputGet = entityAs[UserApiOutput]
          outputGet.id should be (output.id)
        }
      }
    }

    "be able to remove users (DELETE /users)" in {
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        def output = entityAs[UserApiOutput]

        val requestD = Delete(uri = "/users/" + output.id)

        requestD ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)


          entityAs[UserId] should not be (null)
        }
      }
    }

    "be able to update user (GET /users)" in {
      val user = NewUserApiInput("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        def output = entityAs[UserApiOutput]

        val userPut = ReplaceUserApiInput(output.id, "Kapi2", 42, "jp")
        val userEntity = Marshal(userPut).to[MessageEntity].futureValue // futureValue is from ScalaFutures
        val requestPut = Put(uri = "/users/" + output.id).withEntity(userEntity)

        requestPut ~> routes ~> check {
          status should ===(StatusCodes.OK)

          // we expect the response to be json:
          contentType should ===(ContentTypes.`application/json`)


          def outputPut = entityAs[UserApiOutput]
          outputPut.id should be (output.id)
          outputPut.name should be ("Kapi2")
        }
      }
    }
  }
}