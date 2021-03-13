package app.domain

class UserService(repository: UserRepository) {

  def findAll(): Users = repository.findAll

  def save(input: NewUserInput): User = repository.save(input)

  def replace(input: ReplaceUserInput): Option[User] = {
    val user = User.createFrom(input)
    repository.findById(user.id)
      .map(_ => user)
      .map(repository.save)
  }

  def findById(id: UserId): Option[User] = repository.findById(id)

  def deleteById(id: UserId): Option[UserId] = {
    repository.findById(id)
      .map(_.id)
      .flatMap(repository.deleteById)
  }
}