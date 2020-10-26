package app.gateway.out

import app.domain.Users

final case class UsersApiOutput(users: Seq[UserApiOutput])

object UsersApiOutputBuilder {
  def fromDomain(users: Users): UsersApiOutput = UsersApiOutput(users.raw.map(UserApiOutputBuilder.fromDomain))
}