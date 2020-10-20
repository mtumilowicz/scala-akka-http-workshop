package app.domain

trait UserRepository {

  def findAll: Users

  def findById(id: String): Option[User]

  def save(user: User): Unit

  def delete(userName: String): Unit

}
