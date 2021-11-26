package app.domain.user

import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError
import cats.data._
import cats.implicits._

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
    for {
      balance <- repository.findById(payer)
      chargedBalance <- balance.indexOutgoingAmount(amount).toEitherT[Future]
      currentBalance <- repository.save(chargedBalance)
    } yield currentBalance

  private def postIncomingAmount(recipient: UserId, amount: NonNegativeAmount): EitherT[Future, DomainError, UserId] =
    for {
      balance <- repository.findById(recipient)
      creditedBalance = balance.indexIncomingAmount(amount)
      currentBalance <- repository.save(creditedBalance)
    } yield currentBalance

}
