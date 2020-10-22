package app.domain

class UserService(repository: UserRegistry) {

  def findAll(): Users = repository.findAll

  def save(input: NewUserInput): (User, UserRegistry) = {
    val user = User.createFrom(input)

    (user, repository.save(user))
  }

  def replace(input: ReplaceUserInput): (Option[User], UserRegistry) = {
    val user = User.createFrom(input)

    val userOpt = repository.findById(user.id.raw)
      .map(_ => user)

    (userOpt, userOpt
      .map(repository.save)
      .getOrElse(repository))
  }

  def findById(id: String): Option[User] = repository.findById(id)

  def deleteById(id: String): (Option[UserId], UserRegistry) = {
    (Option(UserId(id)), repository.deleteById(id))
  }
}