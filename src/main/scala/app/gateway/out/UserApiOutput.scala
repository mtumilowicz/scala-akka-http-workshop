package app.gateway.out

import app.domain.User

final case class UserApiOutput(id: String, name: String, age: Int)

object UserApiOutputBuilder {
  def fromDomain(user: User): UserApiOutput = UserApiOutput(user.id.raw, user.name, user.age)
}