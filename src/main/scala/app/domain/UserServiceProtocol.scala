package app.domain

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserServiceProtocol {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[User]) extends Command

  final case class ReplaceUser(input: ReplaceUserInput, replyTo: ActorRef[Option[User]]) extends Command

  final case class GetUserById(id: String, replyTo: ActorRef[Option[User]]) extends Command

  final case class DeleteUserById(name: String, replyTo: ActorRef[Option[UserId]]) extends Command

  def apply(): Behavior[Command] = xxx(new UserService(new UserRegistry(Map())))

  def xxx(service: UserService) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! service.findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        val (user, database) = service.save(input)
        replyTo ! user
        xxx(new UserService(database))
      case ReplaceUser(input, replyTo) =>
        val (userOpt, database) = service.replace(input)
        replyTo ! userOpt
        xxx(new UserService(database))
      case GetUserById(id, replyTo) =>
        replyTo ! service.findById(id)
        Behaviors.same
      case DeleteUserById(id, replyTo) =>
        val (userOpt, database) = service.deleteById(id)
        replyTo ! userOpt
        xxx(new UserService(database))
    }
  }
}
