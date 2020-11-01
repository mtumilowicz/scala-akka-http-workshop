package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserServiceProtocol._
import app.domain._
import app.gateway.out.{UserApiOutput, UserApiOutputBuilder, UsersApiOutput, UsersApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userService: ActorRef[UserServiceProtocol.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersApiOutput] =
    userService.ask(GetUsers).map(UsersApiOutputBuilder.fromDomain)

  def getUserById(id: String): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutputBuilder.fromDomain)

    userService.ask(GetUserById(UserId(id), _)).map(toOutput)
  }

  def createUser(input: NewUserInput): Future[UserApiOutput] =
    userService.ask(CreateUser(input, _)).map(UserApiOutputBuilder.fromDomain)

  def replaceUser(input: ReplaceUserInput): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutputBuilder.fromDomain)

    userService.ask(ReplaceUser(input, _)).map(toOutput)
  }

  def deleteUserById(id: String): Future[Option[UserId]] =
    userService.ask(DeleteUserById(UserId(id), _))

}
