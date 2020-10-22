package app.domain

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import app.domain.UserServiceProtocol._

class UserService(repository: UserRepository) {

  def findAll(): Users = repository.findAll

  def save(input: NewUserInput): User = {
    def user = User.createFrom(input)

    repository.save(user)
  }

  def replace(input: ReplaceUserInput): Option[User] = {
    def user = User.createFrom(input)

    repository.findById(user.id.raw)
      .map(_ => user)
      .map(repository.save)
  }

  def findById(name: String): Option[User] = repository.findById(name)

  def deleteById(id: String): Option[UserId] = {
    repository.deleteById(id)
  }

  def behaviour: Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        replyTo ! save(input)
        Behaviors.same
      case ReplaceUser(input, replyTo) =>
        replyTo ! replace(input)
        Behaviors.same
      case GetUserById(name, replyTo) =>
        replyTo ! findById(name)
        Behaviors.same
      case DeleteUserById(name, replyTo) =>
        replyTo ! deleteById(name)
        Behaviors.same
    }
}