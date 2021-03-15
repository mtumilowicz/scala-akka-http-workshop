package app

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.server.Directives._
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}
import app.infrastructure.http.HttpServerConfig
import app.infrastructure.http.user.UserRoutesConfig
import app.infrastructure.http.venue.VenueRoutesConfig

object App {

  def main(args: Array[String]): Unit = {
    val rootBehavior = configureRootBehaviour
    val system = ActorSystem[Nothing](rootBehavior, "UserHttpServer")
  }

  private def configureRootBehaviour: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    implicit val system: ActorSystem[Nothing] = context.system
    val userService = UserConfig.inMemoryService()
    val userServiceActor = context.spawn(UserConfig.actor(userService).behavior(), "UserServiceActor")
    context.watch(userServiceActor)

    val venueService = VenueConfig.inMemoryService()
    val purchaseService = PurchaseConfig.service(userService, venueService)
    val venueActor = context.spawn(VenueConfig.actor(venueService).behavior(), "VenueActor")
    context.watch(venueActor)
    val purchaseActor = context.spawn(PurchaseConfig.actor(purchaseService).behavior(), "PurchaseActor")
    context.watch(purchaseActor)

    val userRoutes = UserRoutesConfig.config(userServiceActor)
    val venueRoutes = VenueRoutesConfig.config(venueActor, purchaseActor)
    val route = userRoutes.route ~ venueRoutes.route

    HttpServerConfig.startHttpServer(route)(context.system)
    Behaviors.empty
  }
}
