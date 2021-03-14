package app.infrastructure.http

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server._
import app.gateway.Routes

object RouteConfig {

  def config(context: ActorContext[Nothing]): Route =
    new Routes(context).routes
}
