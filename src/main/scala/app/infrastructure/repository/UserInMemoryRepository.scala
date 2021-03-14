package app.infrastructure.repository

import app.domain.user
import app.domain.user.error.UserNotFoundError
import app.domain.user._

import scala.collection.mutable

object UserInMemoryRepository extends UserRepository {
  val map: mutable.Map[UserId, User] = mutable.Map()

  def findAll: Users = user.Users(map.values.toSeq)

  def findById(id: UserId): Either[UserNotFoundError, User] =
    map.get(id).toRight(UserNotFoundError(id))

  def save(input: NewUserInput): User =
    save(User.createFrom(UserId(java.util.UUID.randomUUID().toString), input))

  def save(user: User): User = {
    map.put(user.id, user)
    user
  }

  def deleteById(id: UserId): Option[UserId] =
    map.remove(id).map(_.id)
}
