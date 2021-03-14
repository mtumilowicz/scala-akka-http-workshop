package app.domain.user

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object UserServiceProtocolWorkshop {

  def apply(initialBehavior: UserService): Behavior[Command] = become(initialBehavior)

  def become(service: UserService): Behavior[Command] = {
    // hint: Behaviors.receiveMessage
    // hint: case exhaustive
    // hint: object ! actorRef
    // hint: Behaviors.same
    // hint: become(new UserService(...))
    Behaviors.same
  }

  sealed trait Command

  // GetUsers, CreateUser, ReplaceUser, GetUserById, DeleteUserById
  // hint: extends Command
  // hint: CommandType(input: InputTypeIfAny, replyTo: ActorRef[ReturnType])
}
