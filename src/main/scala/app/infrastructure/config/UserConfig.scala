package app.infrastructure.config

import app.domain.user.{UserRepository, UserService}
import app.infrastructure.actor.{UserActor}
import app.infrastructure.repository.UserInMemoryRepository

object UserConfig {

  def inMemoryActor(): UserActor =
    actor(inMemoryService())

  def actor(service: UserService): UserActor =
    new UserActor(service)

  def inMemoryService(): UserService =
    new UserService(inMemoryRepository())

  def inMemoryRepository(): UserRepository =
    UserInMemoryRepository

}
