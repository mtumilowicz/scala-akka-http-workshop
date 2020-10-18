package app.gateway.out

import app.domain.{User, Users}

import scala.collection.immutable

final case class UsersOutput(users: immutable.Seq[UserOutput])

object UsersOutput {
  def fromDomain(users: Users): UsersOutput = UsersOutput(users.users.map(UserOutput.fromDomain))
}