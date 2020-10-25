package app.domain

class UserService(registry: UserRegistry) {

  def findAll(): Users = registry.findAll

  def save(input: NewUserInput): (User, UserRegistry) = registry.save(input)

  def replace(input: ReplaceUserInput): (Option[User], UserRegistry) = {
    val user = User.createFrom(input)

    val userOpt = registry.findById(user.id)
      .map(_ => user)

    (userOpt, userOpt
      .map(registry.save)
      .getOrElse(registry))
  }

  def findById(id: UserId): Option[User] = registry.findById(id)

  def deleteById(id: UserId): (Option[UserId], UserRegistry) = {
    val userId = registry.findById(id)
      .map(_.id)

    (userId, userId
      .map(registry.deleteById)
      .getOrElse(registry))
  }
}