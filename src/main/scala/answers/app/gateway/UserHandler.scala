package answers.app.gateway

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import answers.app.domain.UserServiceProtocol._
import answers.app.domain._
import answers.app.gateway.out.{UserApiOutput, UserApiOutputBuilder, UsersApiOutput, UsersApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserHandler(userRegistry: ActorRef[UserServiceProtocol.Command])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))


  def getUsers(): Future[UsersApiOutput] =
    userRegistry.ask(GetUsers).map(UsersApiOutputBuilder.fromDomain)

  def getUserById(id: String): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutputBuilder.fromDomain)

    userRegistry.ask(GetUserById(UserId(id), _)).map(toOutput)
  }

  def createUser(input: NewUserInput): Future[UserApiOutput] =
    userRegistry.ask(CreateUser(input, _)).map(UserApiOutputBuilder.fromDomain)

  def replaceUser(input: ReplaceUserInput): Future[Option[UserApiOutput]] = {
    def toOutput: Option[User] => Option[UserApiOutput] = _.map(UserApiOutputBuilder.fromDomain)

    userRegistry.ask(ReplaceUser(input, _)).map(toOutput)
  }

  def deleteUserById(id: String): Future[Option[UserId]] =
    userRegistry.ask(DeleteUserById(UserId(id), _))

}
