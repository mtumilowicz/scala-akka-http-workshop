package app.domain

import scala.collection.immutable

class UserRegistry(private val map: immutable.Map[String, User] = immutable.Map()) {

  def findAll: Users = Users(map.values.toSeq)

  def findById(id: String): Option[User] = map.get(id)

  def save(user: User): UserRegistry =
    new UserRegistry(map + (user.id.raw -> user))

  def deleteById(id: String): UserRegistry =
    new UserRegistry(map - id)
}
