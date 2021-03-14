package app.gateway

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.infrastructure.http.user.UserRoutesConfig
import app.infrastructure.http.venue.VenueRoutesConfig

class Routes(val context: ActorContext[Nothing]) {
  val routes: Route = VenueRoutesConfig.config(context).routes ~
    UserRoutesConfig.config(context).userRoutes
}
