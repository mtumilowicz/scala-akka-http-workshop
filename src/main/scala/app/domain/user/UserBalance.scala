package app.domain.user

import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError
import app.domain.user.error.CantAffordTransactionError

case class UserBalance(id: UserId, budget: NonNegativeAmount) {

  def indexIncomingAmount(amount: NonNegativeAmount): UserBalance =
    copy(budget = budget + amount)

  def indexOutgoingAmount(amount: NonNegativeAmount): Either[DomainError, UserBalance] =
    outgoingAmount(amount) match {
      case Right(balance) => Right(copy(budget = balance))
      case Left(_) => Left(CantAffordTransactionError(id))
    }

  private def outgoingAmount(amount: NonNegativeAmount): Either[DomainError, NonNegativeAmount] =
    budget - amount
}
