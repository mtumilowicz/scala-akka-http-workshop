package app.domain

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
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

  def behaviour: Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        replyTo ! save(input)
        Behaviors.same
      case GetUser(name, replyTo) =>
        replyTo ! findById(name)
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! delete(name)
        Behaviors.same
    }
}