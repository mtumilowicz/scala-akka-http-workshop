package app.infrastructure

import akka.actor.typed.scaladsl.ActorContext
import app.gateway.{UserHandler, UserRoutes}

object RoutesConfig {
  def config(context: ActorContext[Nothing]): UserRoutes = {
    val userServiceActor = context.spawn(UserServiceConfiguration.inMemoryBehaviour, "UserServiceActor")
    context.watch(userServiceActor)

    new UserRoutes(new UserHandler(userServiceActor)(context.system))
  }
}
