package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserServiceProtocol._
import app.domain.{NewUserInput, User, UserServiceProtocol}
import app.gateway.out.{UserApiOutput, UsersApiOutput}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userRegistry: ActorRef[UserServiceProtocol.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersApiOutput] =
    userRegistry.ask(GetUsers).map(UsersApiOutput.fromDomain)

  def getUser(name: String): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutput.fromDomain)
    userRegistry.ask(GetUser(name, _)).map(toOutput)
  }

  def createUser(newUserInput: NewUserInput): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(newUserInput, _))

  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

}
