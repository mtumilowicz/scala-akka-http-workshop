package app

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import app.infrastructure.http.{HttpServerConfig, RouteConfig}
import app.infrastructure.http.user.UserRoutesConfig

object App {

  def main(args: Array[String]): Unit = {
    val rootBehavior = configureRootBehaviour
    val system = ActorSystem[Nothing](rootBehavior, "UserHttpServer")
  }

  private def configureRootBehaviour: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val route = RouteConfig.config(context)
    HttpServerConfig.startHttpServer(route)(context.system)
    Behaviors.empty
  }
}
