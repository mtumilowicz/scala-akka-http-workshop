package app.infrastructure

import app.domain.UserRepository

object UserRepositoryConfiguration {
  def inMemory(): UserRepository = new UserRepositoryInMemory()
}
