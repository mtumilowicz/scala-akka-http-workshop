package app.domain

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import app.domain.UserServiceProtocol._

class UserService(repository: UserRepository) {

  def behaviour(): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! repository.findAll
        Behaviors.same
      case CreateUser(input, replyTo) =>
        replyTo ! ActionPerformed(s"User ${input.name} created.")
        repository.save(User.createFrom(input))
        Behaviors.same
      case GetUser(name, replyTo) =>
        replyTo ! repository.findById(name)
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! ActionPerformed(s"User $name deleted.")
        repository.delete(name)
        Behaviors.same
    }
}