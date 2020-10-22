package app.infrastructure

import app.domain.{User, UserId, Users}

import scala.collection.mutable

object UserDatabase {

  private val map: mutable.Map[String, User] = mutable.Map()

  def findAll: Users = Users(map.values.toSeq)

  def findById(id: String): Option[User] = map.get(id)

  def save(user: User): User = {
    map.put(user.id.raw, user)
    user
  }

  def deleteById(id: String): Option[UserId] = {
    map.remove(id).map(_.id)
  }

}
