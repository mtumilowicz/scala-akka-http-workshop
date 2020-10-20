package app.domain

import app.domain.UserServiceProtocol._

class UserService(repository: UserRepository) {

  def findAll(): Users = repository.findAll

  def save(input: NewUserInput): ActionPerformed = {
    repository.save(User.createFrom(input))
    ActionPerformed(s"User ${input.name} created.")
  }

  def findById(name: String): Option[User] = repository.findById(name)

  def delete(name: String): ActionPerformed = {
    repository.delete(name)
    ActionPerformed(s"User $name deleted.")
  }

}