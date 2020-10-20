package app.infrastructure

import akka.actor.typed.Behavior
import app.domain.{UserService, UserServiceProtocol}

object UserServiceConfiguration {

  def inMemory: UserService = new UserService(UserRepositoryConfiguration.inMemory())

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] =
    UserServiceProtocol.behaviour(inMemory)

}
