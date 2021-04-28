package app.domain.user

import cats.data._
import cats.implicits._
import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserBalanceService(val repository: UserBalanceRepository) {

  def save(user: UserBalance): EitherT[Future, DomainError, UserId] =
    repository.save(user)

  def findById(userId: UserId): EitherT[Future, DomainError, UserBalance] =
    repository.findById(userId)

  def transfer(recipient: UserId, payer: UserId, amount: NonNegativeAmount): EitherT[Future, DomainError, UserId] =
    postOutgoingAmount(payer, amount)
      .flatMap(_ => postIncomingAmount(recipient, amount))

  def postOutgoingAmount(payer: UserId, amount: NonNegativeAmount): EitherT[Future, DomainError, UserId] =
    repository.findById(payer)
      .flatMap(_.indexOutgoingAmount(amount).toEitherT)
      .flatMap(repository.save)

  private def postIncomingAmount(recipient: UserId, amount: NonNegativeAmount): EitherT[Future, DomainError, UserId] =
    repository.findById(recipient)
      .map(_.indexIncomingAmount(amount))
      .flatMap(repository.save)

}
