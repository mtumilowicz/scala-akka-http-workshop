package app.domain

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserServiceProtocol {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUser(name: String, replyTo: ActorRef[Option[User]]) extends Command

  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class ActionPerformed(description: String)

  def behaviour(userService: UserService): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! userService.findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        replyTo ! userService.save(input)
        Behaviors.same
      case GetUser(name, replyTo) =>
        replyTo ! userService.findById(name)
        Behaviors.same
      case DeleteUser(name, replyTo) =>
        replyTo ! userService.delete(name)
        Behaviors.same
    }

}
