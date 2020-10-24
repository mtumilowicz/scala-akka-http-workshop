package app.domain

import scala.collection.immutable

class UserRegistry(private val map: immutable.Map[UserId, User] = immutable.Map()) {

  def findAll: Users = Users(map.values.toSeq)

  def findById(id: UserId): Option[User] = map.get(id)

  def save(user: User): UserRegistry =
    new UserRegistry(map + (user.id -> user))

  def deleteById(id: UserId): UserRegistry =
    new UserRegistry(map - id)
}
