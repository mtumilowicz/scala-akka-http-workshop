package app.infrastructure

import akka.actor.typed.Behavior
import app.domain.{UserService, UserServiceProtocol}

object UserServiceConfiguration {

  def inMemoryBehaviour: Behavior[UserServiceProtocol.Command] = UserServiceProtocol()

}
