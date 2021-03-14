package app.gateway.user.out

import app.domain.user.Users

final case class UsersApiOutput(users: Seq[UserApiOutput])

object UsersApiOutputBuilder {
  def fromDomain(users: Users): UsersApiOutput = UsersApiOutput(users.raw.map(UserApiOutputBuilder.fromDomain))
}