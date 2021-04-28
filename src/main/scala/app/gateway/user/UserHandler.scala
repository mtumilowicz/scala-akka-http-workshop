package app.gateway.user

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.error.DomainError
import app.domain.user._
import app.gateway.user.out.{UserApiOutput, UserApiOutputBuilder, UsersApiOutput, UsersApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userService: UserService)(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers: Future[UsersApiOutput] =
    Future(userService.findAll()).map(UsersApiOutputBuilder.fromDomain)

  def getUserById(id: String): Future[Either[String, UserApiOutput]] =
    Future(userService.findById(UserId(id)))
    .map(_.map(UserApiOutputBuilder.fromDomain))
    .map(domainErrorAsString)

  def createUser(input: NewUserInput): Future[UserApiOutput] =
    Future(userService.save(input)).map(UserApiOutputBuilder.fromDomain)

  def replaceUser(input: ReplaceUserInput): Future[Either[String, UserApiOutput]] =
    Future(userService.replace(input))
    .map(_.map(UserApiOutputBuilder.fromDomain))
    .map(domainErrorAsString)

  def deleteUserById(id: String): Future[Option[UserId]] =
    Future(userService.deleteById(UserId(id)))

  def domainErrorAsString[B](either: Either[DomainError, B]): Either[String, B] =
    either.left.map(_.message())
}
