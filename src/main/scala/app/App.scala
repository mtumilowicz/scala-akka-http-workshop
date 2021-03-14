package app

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import app.infrastructure.http.{HttpServerConfig, UserRoutesConfig}

object App {

  def main(args: Array[String]): Unit = {
    val rootBehavior = configureRootBehaviour
    val system = ActorSystem[Nothing](rootBehavior, "UserHttpServer")
  }

  private def configureRootBehaviour: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val routes = UserRoutesConfig.config(context)
    HttpServerConfig.startHttpServer(routes.userRoutes)(context.system)
    Behaviors.empty
  }
}
