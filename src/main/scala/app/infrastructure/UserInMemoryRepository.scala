package app.infrastructure

import app.domain.{user, _}
import app.domain.user.{NewUserInput, User, UserId, UserRepository, Users}

import scala.collection.mutable

object UserInMemoryRepository extends UserRepository {
  val map: mutable.Map[UserId, User] = mutable.Map()

  def findAll: Users = user.Users(map.values.toSeq)

  def findById(id: UserId): Option[User] = map.get(id)

  def save(input: NewUserInput): User =
    save(User.createFrom(UserId(java.util.UUID.randomUUID().toString), input))

  def save(user: User): User = {
    map.put(user.id, user)
    user
  }

  def deleteById(id: UserId): Option[UserId] =
    map.remove(id).map(_.id)
}
