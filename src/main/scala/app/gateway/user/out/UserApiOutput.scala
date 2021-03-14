package app.gateway.user.out

import app.domain.user.User

final case class UserApiOutput(id: String, name: String, budget: Int)

object UserApiOutputBuilder {
  def fromDomain(user: User): UserApiOutput =
    UserApiOutput(user.id.raw, user.name, user.budget.raw)
}