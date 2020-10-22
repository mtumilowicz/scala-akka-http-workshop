package app.domain

import akka.actor.typed.ActorRef

object UserServiceProtocol {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[User]) extends Command

  final case class ReplaceUser(input: ReplaceUserInput, replyTo: ActorRef[Option[User]]) extends Command

  final case class GetUserById(id: String, replyTo: ActorRef[Option[User]]) extends Command

  final case class DeleteUserById(name: String, replyTo: ActorRef[Option[UserId]]) extends Command
}
