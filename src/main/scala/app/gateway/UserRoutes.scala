package app.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import com.typesafe.config.ConfigFactory

class UserRoutes(userHandler: UserHandler) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import app.infrastructure.JsonFormats._

  private val resources = ConfigFactory.load()

  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              complete(userHandler.getUsers())
            },
            post {
              entity(as[NewUserApiInput]) { user =>
                onSuccess(userHandler.createUser(user.toDomain)) { user => {
                  val host = Location(resources.getString("my-app.server.host") + s"/users/${user.id}")
                  complete((StatusCodes.Created, Seq(host), user))
                }
                }
              }
            })
        },
        path(Segment) { id =>
          concat(
            get {
              onSuccess(userHandler.getUserById(id)) {
                case Some(value) => complete(value)
                case None => complete(StatusCodes.NotFound, s"user with given id: $id not found")
              }
            },
            delete {
              onSuccess(userHandler.deleteUserById(id)) {
                case Some(value) => complete(value)
                case None => complete(StatusCodes.NotFound, s"user with given id: $id not found")
              }
            },
            put {
              entity(as[ReplaceUserApiInput]) { user =>
                onSuccess(userHandler.replaceUser(user.toDomain(id))) {
                  case Some(value) => complete(value)
                  case None => complete(StatusCodes.NotFound, s"user with given id: $id not found")
                }
              }
            }
          )
        })
    }
}
