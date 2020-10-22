package app.domain

trait UserRepository {

  def findAll: Users

  def findById(id: String): Option[User]

  def save(user: User): User

  def deleteById(id: String): Option[UserId]

}
