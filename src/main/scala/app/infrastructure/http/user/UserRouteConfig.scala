package app.infrastructure.http.user

import akka.actor.typed.{ActorRef, ActorSystem}
import app.gateway.user.{UserHandler, UserRoute}
import app.infrastructure.actor.UserActor

object UserRouteConfig {

  def config(userActor: ActorRef[UserActor.UserCommand])
            (implicit system: ActorSystem[_]): UserRoute = {
    new UserRoute(new UserHandler(userActor))
  }
}
