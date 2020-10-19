package app.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.domain.User
import app.gateway.in.NewUserApiInput

//#import-json-formats
//#user-routes-class
class UserRoutes(userHandler: UserHandler) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import app.infrastructure.JsonFormats._
  //#import-json-formats

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
    pathPrefix("users") {
      concat(
        //#users-get-delete
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
        //#users-get-delete
        //#users-get-post
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              rejectEmptyResponse {
                onSuccess(userHandler.getUser(name)) { response =>
                  complete(response)
                }
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              onSuccess(userHandler.deleteUser(name)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            })
        })
      //#users-get-delete
    }
  //#all-routes
}
