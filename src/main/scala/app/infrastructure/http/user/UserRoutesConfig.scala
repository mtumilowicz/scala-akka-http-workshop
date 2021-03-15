package app.infrastructure.http.user

import akka.actor.typed.{ActorRef, ActorSystem}
import app.gateway.user.{UserHandler, UserRoutes}
import app.infrastructure.actor.UserActor

object UserRoutesConfig {

  def config(userActor: ActorRef[UserActor.UserCommand])
            (implicit system: ActorSystem[_]): UserRoutes = {
    new UserRoutes(new UserHandler(userActor))
  }
}
