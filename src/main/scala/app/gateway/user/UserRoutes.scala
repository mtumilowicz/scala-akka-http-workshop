package app.gateway.user

import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.gateway.user.in.{NewUserApiInput, ReplaceUserApiInput}
import app.infrastructure.http.user.UserJsonFormats._
import com.typesafe.config.ConfigFactory

class UserRoutes(userHandler: UserHandler) {

  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            get {
              complete(userHandler.getUsers)
            },
            post {
              entity(as[NewUserApiInput]) { user =>
                onSuccess(userHandler.createUser(user.toDomain)) { user => {
                  val uri = Uri.from(
                    scheme = "http",
                    host = resources.getString("my-app.server.host"),
                    port = resources.getInt("my-app.server.port"),
                    path = s"/users/${user.id}"
                  )
                  val location = Location(uri)
                  complete(StatusCodes.Created, Seq(location), user)
                }
                }
              }
            })
        },
        path(Segment) { id =>
          concat(
            get {
              onSuccess(userHandler.getUserById(id)) {
                case Right(value) => complete(value)
                case Left(error) => complete(StatusCodes.NotFound, error)
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
                  case Right(value) => complete(value)
                  case Left(error) => complete(StatusCodes.NotFound, error)
                }
              }
            }
          )
        })
    }
  private val resources = ConfigFactory.load()
}
