package app.infrastructure

import akka.actor.typed.scaladsl.ActorContext
import app.domain.UserService
import app.gateway.{UserHandler, UserRoutes}

object RoutesConfig {
  def config(context: ActorContext[Nothing]): UserRoutes = {
    val userRegistryActor = context.spawn(new UserService(UserRepositoryConfiguration.inMemory()).behaviour(), "UserRegistryActor")
    context.watch(userRegistryActor)

    new UserRoutes(new UserHandler(userRegistryActor)(context.system))
  }
}
