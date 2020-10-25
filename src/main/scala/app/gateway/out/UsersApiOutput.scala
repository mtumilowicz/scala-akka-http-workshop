package app.gateway.out

import app.domain.Users

final case class UsersApiOutput(users: Seq[UserApiOutput])

object UsersApiOutput {
  def fromDomain(users: Users): UsersApiOutput = UsersApiOutput(users.raw.map(UserApiOutput.fromDomain))
}