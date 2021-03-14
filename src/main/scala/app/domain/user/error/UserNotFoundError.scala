package app.domain.user.error

import app.domain.error.DomainError
import app.domain.user.UserId

case class UserNotFoundError(userId: UserId) extends DomainError {
  override def message(): String =
    s"${userId.raw} not found."
}
