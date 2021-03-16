package app.user

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import app.domain.user.UserId
import app.gateway.user.in.NewUserApiInput
import app.gateway.user.out.UserApiOutput
import app.infrastructure.http.JsonFormats._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

object UserTestFacade extends AnyWordSpec with ScalaFutures with ScalatestRouteTest {

  def createUser(budget: Int)(implicit route: Route): UserApiOutput = {
    val user = NewUserApiInput("Kapi", budget)
    val userEntity = Marshal(user).to[MessageEntity].futureValue

    val request = Post("/users").withEntity(userEntity)

    request ~> route ~> check {
      entityAs[UserApiOutput]
    }
  }

  def getUser(id: UserId)(implicit route: Route): UserApiOutput = {
    val get = Get(uri = "/users/" + id.raw)

    get ~> route ~> check {
      entityAs[UserApiOutput]
    }
  }

}
