package app.domain

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserServiceProtocol {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[User]) extends Command

  final case class ReplaceUser(input: ReplaceUserInput, replyTo: ActorRef[Option[User]]) extends Command

  final case class GetUserById(id: UserId, replyTo: ActorRef[Option[User]]) extends Command

  final case class DeleteUserById(name: UserId, replyTo: ActorRef[Option[UserId]]) extends Command

  def apply(initialBehavior: UserService): Behavior[Command] = behavior(initialBehavior)

  def behavior(service: UserService) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! service.findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        val (user, database) = service.save(input)
        replyTo ! user
        behavior(new UserService(database))
      case ReplaceUser(input, replyTo) =>
        val (userOpt, database) = service.replace(input)
        replyTo ! userOpt
        behavior(new UserService(database))
      case GetUserById(id, replyTo) =>
        replyTo ! service.findById(id)
        Behaviors.same
      case DeleteUserById(id, replyTo) =>
        val (userOpt, database) = service.deleteById(id)
        replyTo ! userOpt
        behavior(new UserService(database))
    }
  }
}
