package app.infrastructure.http.user

import akka.actor.typed.{ActorSystem}
import app.domain.user.UserService
import app.gateway.user.{UserHandler, UserRoute}

object UserRouteConfig {

  def config(userService: UserService)
            (implicit system: ActorSystem[_]): UserRoute = {
    new UserRoute(new UserHandler(userService))
  }
}
