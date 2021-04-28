package app.domain.purchase.error

import app.domain.error.DomainError
import app.domain.user.UserId

case class CantBuyFromYourselfError(userId: UserId) extends DomainError