package app.infrastructure

import akka.actor.typed.Behavior
import app.domain.{UserService, UserServiceProtocol}

object UserServiceConfiguration {

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] = UserServiceProtocol(inMemoryService)

  def inMemoryService: UserService = new UserService(new UserInMemoryRegistry())

}
