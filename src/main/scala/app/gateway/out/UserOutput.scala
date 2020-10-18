package app.gateway.out

import app.domain.User

final case class UserOutput(name: String, age: Int)

object UserOutput {
  def fromDomain(user: User): UserOutput = UserOutput(user.name, user.age)
}