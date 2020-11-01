package app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.UserServiceProtocolWorkshop._
import app.domain._
import app.gateway.out.{UserApiOutput, UserApiOutputBuilder, UsersApiOutput, UsersApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandlerWorkshop(userService: ActorRef[UserServiceProtocolWorkshop.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

//   def getUsers(): Future[UsersApiOutput] =
//   hint: userService, ask, map

//   def getUserById(id: String): Future[Option[UserApiOutput]] = {
//   hint: userService, ask, map

//  def createUser(input: NewUserInput): Future[UserApiOutput] =
//  hint: userService, ask, map

//  def replaceUser(input: ReplaceUserInput): Future[Option[UserApiOutput]] = {
//  hint: userService, ask, map

//  def deleteUserById(id: String): Future[Option[UserId]] =
//  hint: userService, ask

}
