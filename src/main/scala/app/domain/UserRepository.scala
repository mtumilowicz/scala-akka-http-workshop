package app.domain

trait UserRepository {

  def findAll: Users

  def findById(id: UserId): Option[User]

  def save(input: NewUserInput): User

  def save(user: User): User

  def deleteById(id: UserId): Option[UserId]
}
