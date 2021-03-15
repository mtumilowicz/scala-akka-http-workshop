package app.gateway.user

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.error.DomainError
import app.domain.user._
import app.gateway.user.out.{UserApiOutput, UserApiOutputBuilder, UsersApiOutput, UsersApiOutputBuilder}
import app.infrastructure.actor.UserActor
import app.infrastructure.actor.UserActor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userService: ActorRef[UserActor.UserCommand])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers: Future[UsersApiOutput] =
    userService.ask(GetUsers).map(UsersApiOutputBuilder.fromDomain)

  def getUserById(id: String): Future[Either[String, UserApiOutput]] = {
    userService.ask(GetUserById(UserId(id), _))
      .map(_.map(UserApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)
  }

  def domainErrorAsString[B](either: Either[DomainError, B]): Either[String, B] =
    either.left.map(_.message())

  def createUser(input: NewUserInput): Future[UserApiOutput] =
    userService.ask(CreateUser(input, _)).map(UserApiOutputBuilder.fromDomain)

  def replaceUser(input: ReplaceUserInput): Future[Either[String, UserApiOutput]] = {
    userService.ask(ReplaceUser(input, _))
      .map(_.map(UserApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)
  }

  def deleteUserById(id: String): Future[Option[UserId]] =
    userService.ask(DeleteUserById(UserId(id), _))
}
