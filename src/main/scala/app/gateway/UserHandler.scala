package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserServiceProtocol._
import app.domain.{NewUserInput, User, UserId, UserServiceProtocol}
import app.gateway.out.{UserApiOutput, UsersApiOutput}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userRegistry: ActorRef[UserServiceProtocol.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersApiOutput] =
    userRegistry.ask(GetUsers).map(UsersApiOutput.fromDomain)

  def getUserById(id: String): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutput.fromDomain)
    userRegistry.ask(GetUserById(id, _)).map(toOutput)
  }

  def createUser(newUserInput: NewUserInput): Future[UserApiOutput] =
    userRegistry.ask(CreateUser(newUserInput, _)).map(UserApiOutput.fromDomain)

  def deleteUserById(id: String): Future[Option[UserId]] =
    userRegistry.ask(DeleteUserById(id, _))

}
