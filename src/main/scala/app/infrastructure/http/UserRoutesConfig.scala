package app.infrastructure.http

import akka.actor.typed.scaladsl.ActorContext
import app.gateway.user.{UserHandler, UserHandlerWorkshop, UserRoutes, UserRoutesWorkshop}
import app.infrastructure.config.UserConfig

object UserRoutesConfig {
  def config(context: ActorContext[Nothing]): UserRoutes = {
    val userServiceActor = context.spawn(UserConfig.inMemoryBehaviour(), "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutes(new UserHandler(userServiceActor)(context.system))
  }

  def configWorkshop(context: ActorContext[Nothing]): UserRoutesWorkshop = {
    val userServiceActor = context.spawn(UserConfig.workshopBehaviour(), "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutesWorkshop(new UserHandlerWorkshop(userServiceActor)(context.system))
  }
}
