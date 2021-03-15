package app

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import app.infrastructure.http.{HttpServerConfig, RouteConfig}

object App {

  def main(args: Array[String]): Unit = {
    val rootBehavior = configureRootBehaviour
    val system = ActorSystem[Nothing](rootBehavior, "UserHttpServer")
  }

  private def configureRootBehaviour: Behavior[Nothing] = Behaviors.setup[Nothing] { implicit context =>
    implicit val system = context.system
    val route = RouteConfig.inMemoryRoute

    HttpServerConfig.startHttpServer(route)
    Behaviors.empty
  }
}
