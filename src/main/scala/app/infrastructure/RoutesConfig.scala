package app.infrastructure

import akka.actor.typed.scaladsl.ActorContext
import app.gateway.{UserHandler, UserHandlerWorkshop, UserRoutes, UserRoutesWorkshop}

object RoutesConfig {
  def config(context: ActorContext[Nothing]): UserRoutes = {
    val userServiceActor = context.spawn(UserServiceConfiguration.inMemoryBehaviour, "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutes(new UserHandler(userServiceActor)(context.system))
  }

  def configWorkshop(context: ActorContext[Nothing]): UserRoutesWorkshop = {
    val userServiceActor = context.spawn(UserServiceConfiguration.workshopBehaviour, "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutesWorkshop(new UserHandlerWorkshop(userServiceActor)(context.system))
  }
}
