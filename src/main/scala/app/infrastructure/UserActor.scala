package app.infrastructure

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.domain.error.DomainError
import app.domain.user._
import app.infrastructure.UserActor._

class UserActor(userService: UserService) {

  def behavior(): Behavior[UserCommand] = {
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! userService.findAll()
        Behaviors.same
      case CreateUser(input, replyTo) =>
        replyTo ! userService.save(input)
        Behaviors.same
      case ReplaceUser(input, replyTo) =>
        replyTo ! userService.replace(input)
        Behaviors.same
      case GetUserById(id, replyTo) =>
        replyTo ! userService.findById(id)
        Behaviors.same
      case DeleteUserById(id, replyTo) =>
        replyTo ! userService.deleteById(id)
        Behaviors.same
    }
  }
}

object UserActor {

  sealed trait UserCommand

  final case class GetUsers(replyTo: ActorRef[Users]) extends UserCommand

  final case class CreateUser(input: NewUserInput, replyTo: ActorRef[User]) extends UserCommand

  final case class ReplaceUser(input: ReplaceUserInput, replyTo: ActorRef[Either[DomainError, User]]) extends UserCommand

  final case class GetUserById(id: UserId, replyTo: ActorRef[Either[DomainError, User]]) extends UserCommand

  final case class DeleteUserById(name: UserId, replyTo: ActorRef[Option[UserId]]) extends UserCommand

}
