package app.gateway.out

import app.domain.User

final case class UserApiOutput(name: String, age: Int)

object UserApiOutput {
  def fromDomain(user: User): UserApiOutput = UserApiOutput(user.name, user.age)
}