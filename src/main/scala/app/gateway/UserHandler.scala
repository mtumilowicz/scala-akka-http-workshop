package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserRegistry._
import app.domain.{NewUserCommand, User, UserRegistry}
import app.gateway.out.{UserApiOutput, UsersApiOutput}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersApiOutput] =
    userRegistry.ask(GetUsers).map(UsersApiOutput.fromDomain)

  def getUser(name: String): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutput.fromDomain)
    userRegistry.ask(GetUser(name, _)).map(toOutput)
  }

  def createUser(newUserCommand: NewUserCommand): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(User.createFrom(newUserCommand), _))

  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

}
