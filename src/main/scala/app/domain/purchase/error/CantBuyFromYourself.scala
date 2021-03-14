package app.domain.purchase.error

import app.domain.error.DomainError
import app.domain.user.UserId

case class CantBuyFromYourself(userId: UserId) extends DomainError {
  override def message(): String =
    s"User ${userId.raw} cannot buy from himself."
}
