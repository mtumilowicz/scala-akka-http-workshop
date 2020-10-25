package app.domain

trait UserRegistry {

  def findAll: Users

  def findById(id: UserId): Option[User]

  def save(input: NewUserInput): (User, UserRegistry)

  def save(user: User): UserRegistry

  def deleteById(id: UserId): UserRegistry
}
