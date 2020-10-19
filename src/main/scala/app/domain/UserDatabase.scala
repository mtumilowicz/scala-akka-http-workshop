package app.domain

import scala.collection.mutable

object UserDatabase {

  private val map: mutable.Map[String, User] = mutable.Map()

  def findAll: Users = Users(map.values.toSeq)

  def findById(id: String): Option[User] = map.get(id)

  def save(user: User): Unit = {
    map.put(user.name, user)
  }

  def delete(userName: String): Unit = {
    map.remove(userName)
  }

}
