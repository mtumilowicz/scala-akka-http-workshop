package app.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.gateway.in.NewUserApiInput

class UserRoutes(userHandler: UserHandler) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import app.infrastructure.JsonFormats._

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
                onSuccess(userHandler.createUser(user.toDomain)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { name =>
          concat(
            get {
              onSuccess(userHandler.getUser(name)) {
                case Some(value) => complete(value)
                case None => complete(StatusCodes.NotFound, s"user with given name: $name not found")
              }
            },
            delete {
              onSuccess(userHandler.deleteUser(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
            })
        })
    }
}
