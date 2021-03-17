package app.domain.user

import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError
import app.domain.user.error.CantAffordTransactionError

import scala.util.Try

final case class User(id: UserId, name: String, budget: NonNegativeAmount) {
  def indexIncomingAmount(amount: NonNegativeAmount): User =
    copy(budget = budget + amount)

  def indexOutgoingAmount(amount: NonNegativeAmount): Either[DomainError, User] =
    outgoingAmount(amount)
      .map(balance => copy(budget = balance))
      .fold(_ => Left(CantAffordTransactionError(id)), Right(_))

  private def outgoingAmount(amount: NonNegativeAmount): Try[NonNegativeAmount] =
    Try(budget - amount)
}

object User {
  def createFrom(userId: UserId, input: NewUserInput): User =
    User(userId, input.name, input.budget)

  def createFrom(input: ReplaceUserInput): User =
    User(input.id, input.name, input.budget)
}