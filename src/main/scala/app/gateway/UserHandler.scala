package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserRegistry._
import app.domain.{User, UserRegistry, Users}
import app.gateway.out.UsersOutput

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserHandler(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersOutput] =
    userRegistry.ask(GetUsers).map(UsersOutput.fromDomain)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

}
