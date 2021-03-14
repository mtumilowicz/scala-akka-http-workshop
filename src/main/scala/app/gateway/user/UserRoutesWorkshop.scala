package app.gateway.user

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory

class UserRoutesWorkshop(userHandler: UserHandlerWorkshop) {

  val userRoutes: Route = {
    pathPrefix("users") {
      concat(
        pathEnd {
          concat(
            // hint: get, complete, userHandler
            // hint: post, entity, as, userHandler, Location, complete(status, headers, entity)
          )
        },
        path(Segment) { id =>
          concat(
            // get, onSuccess, userHandler, exhaustive, complete(status, message)
            // delete, onSuccess, userHandler, exhaustive, complete(status, message)
            // put, entity, as, onSuccess, userHandler, exhaustive, complete(status, message)
          )
        })
    }
  }
  private val resources = ConfigFactory.load()
}
