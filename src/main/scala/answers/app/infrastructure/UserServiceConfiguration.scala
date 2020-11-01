package answers.app.infrastructure

import akka.actor.typed.Behavior
import answers.app.domain.{UserService, UserServiceProtocol}

object UserServiceConfiguration {

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] = UserServiceProtocol(inMemoryService)

  def inMemoryService: UserService = new UserService(new UserInMemoryRegistry())

}
