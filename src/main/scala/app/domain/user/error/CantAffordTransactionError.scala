package app.domain.user.error

import app.domain.error.DomainError
import app.domain.user.UserId

case class CantAffordTransactionError(userId: UserId) extends DomainError {
  override def message(): String =
    s"${userId.raw} can't afford that transaction."
}
