package app.infrastructure.config

import akka.actor.typed.Behavior
import app.domain.user.{UserRepository, UserService, UserServiceProtocolWorkshop}
import app.infrastructure.actor.UserActor
import app.infrastructure.repository.UserInMemoryRepository

object UserConfig {

  def actor(service: UserService): UserActor =
    new UserActor(service)

  def inMemoryActor(): UserActor =
    new UserActor(inMemoryService())

  def workshopBehaviour(): Behavior[UserServiceProtocolWorkshop.Command] =
    UserServiceProtocolWorkshop(inMemoryService())

  def inMemoryRepository(): UserRepository =
    UserInMemoryRepository

  def inMemoryService(): UserService =
    new UserService(inMemoryRepository())

}
