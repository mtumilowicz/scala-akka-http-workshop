package app.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.domain.ReplaceUserInput
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}

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
                onSuccess(userHandler.createUser(user.toDomain)) { user =>
                  complete((StatusCodes.Created, user))
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
