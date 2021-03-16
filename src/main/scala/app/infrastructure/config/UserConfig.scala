package app.infrastructure.config

import app.domain.user.{UserRepository, UserService}
import app.infrastructure.actor.{UserActor, UserActorWorkshop}
import app.infrastructure.repository.UserInMemoryRepository

object UserConfig {

  def inMemoryActor(): UserActor =
    actor(inMemoryService())

  def inMemoryActorWorkshop(): UserActor =
    actorWorkshop(inMemoryService())

  def actor(service: UserService): UserActor =
    new UserActor(service)

  def actorWorkshop(service: UserService): UserActor =
    new UserActorWorkshop(service)

  def inMemoryService(): UserService =
    new UserService(inMemoryRepository())

  def inMemoryRepository(): UserRepository =
    UserInMemoryRepository

}
