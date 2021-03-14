package app.infrastructure

import akka.actor.typed.Behavior
import app.domain.user.{UserActor, UserRepository, UserService, UserServiceProtocolWorkshop}

object UserServiceConfiguration {

  def inMemoryBehaviour: Behavior[UserActor.UserCommand] = new UserActor(inMemoryService).behavior()

  def workshopBehaviour: Behavior[UserServiceProtocolWorkshop.Command] = UserServiceProtocolWorkshop(inMemoryService)

  def inMemoryRepository: UserRepository = UserInMemoryRepository

  def inMemoryService: UserService = new UserService(inMemoryRepository)

}
