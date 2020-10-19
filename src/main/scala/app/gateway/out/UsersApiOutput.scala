package app.gateway.out

import app.domain.Users

import scala.collection.immutable

final case class UsersApiOutput(users: immutable.Seq[UserApiOutput])

object UsersApiOutput {
  def fromDomain(users: Users): UsersApiOutput = UsersApiOutput(users.users.map(UserApiOutput.fromDomain))
}