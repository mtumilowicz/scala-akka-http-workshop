package app.domain.user

import cats.data.EitherT
import app.domain.error.DomainError

import scala.concurrent.Future

trait UserBalanceRepository {
  def findById(userId: UserId): EitherT[Future, DomainError, UserBalance]

  def save(userBalance: UserBalance): EitherT[Future, DomainError, UserId]
}
