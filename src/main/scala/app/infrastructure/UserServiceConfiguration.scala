package app.infrastructure

import akka.actor.typed.Behavior
import app.domain.{UserService, UserServiceProtocol, UserServiceProtocolWorkshop}

object UserServiceConfiguration {

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] = UserServiceProtocol(inMemoryService)

  def workshopBehaviour: Behavior[UserServiceProtocolWorkshop.Command] = UserServiceProtocolWorkshop(inMemoryService)

  def inMemoryService: UserService = new UserService(new UserInMemoryRegistry())

}
