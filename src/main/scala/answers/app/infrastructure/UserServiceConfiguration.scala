package answers.app.infrastructure

import akka.actor.typed.Behavior
import answers.app.domain.{UserService, UserServiceProtocol}

object UserServiceConfiguration {

  def inMemoryService: UserService = new UserService(new UserInMemoryRegistry())

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] = UserServiceProtocol(inMemoryService)

}
