package app.infrastructure.config

import app.domain.user.{UserBalanceRepository, UserBalanceService}
import app.infrastructure.repository.UserInMemoryBalanceRepository

object UserBalanceConfig {

  def inMemoryService(): UserBalanceService =
    new UserBalanceService(inMemoryRepository())

  def inMemoryRepository(): UserBalanceRepository =
    new UserInMemoryBalanceRepository()

}
