package app.infrastructure.http.user

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ActorRef, ActorSystem}
import app.gateway.user.{UserHandler, UserHandlerWorkshop, UserRoutes, UserRoutesWorkshop}
import app.infrastructure.actor.UserActor
import app.infrastructure.config.UserConfig

object UserRoutesConfig {

  def config(userActor: ActorRef[UserActor.UserCommand])
            (implicit system: ActorSystem[_]): UserRoutes = {
    new UserRoutes(new UserHandler(userActor))
  }

  def configWorkshop(context: ActorContext[Nothing]): UserRoutesWorkshop = {
    val userServiceActor = context.spawn(UserConfig.workshopBehaviour(), "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutesWorkshop(new UserHandlerWorkshop(userServiceActor)(context.system))
  }
}
