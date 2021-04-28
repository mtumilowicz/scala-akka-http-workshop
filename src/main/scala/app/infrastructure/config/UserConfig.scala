package app.infrastructure.config

import app.domain.user.{UserRepository, UserService}
import app.infrastructure.repository.UserInMemoryRepository

object UserConfig {

  def inMemoryService(): UserService =
    new UserService(inMemoryRepository())

  def inMemoryRepository(): UserRepository =
    UserInMemoryRepository

}
