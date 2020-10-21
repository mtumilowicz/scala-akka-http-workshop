package app.domain

import akka.actor.typed.ActorRef

object UserServiceProtocol {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUser(name: String, replyTo: ActorRef[Option[User]]) extends Command

  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class ActionPerformed(description: String)

}
