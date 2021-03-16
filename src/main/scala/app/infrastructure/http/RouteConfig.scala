package app.infrastructure.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.infrastructure.actor.{PurchaseActor, UserActor, VenueActor}
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}
import app.infrastructure.http.user.UserRouteConfig
import app.infrastructure.http.venue.VenueRouteConfig

object RouteConfig {

  def inMemoryRoute(implicit context: ActorContext[Nothing]): Route = {
    implicit val system = context.system
    val userService = UserConfig.inMemoryService()
    val venueService = VenueConfig.inMemoryService()
    val purchaseService = PurchaseConfig.service(userService, venueService)

    route(UserConfig.actor(userService),
      VenueConfig.actor(venueService),
      PurchaseConfig.actor(purchaseService)
    )
  }

  private def route(userActor: UserActor,
                    venueActor: VenueActor,
                    purchaseActor: PurchaseActor)
                   (implicit context: ActorContext[Nothing]): Route = {
    implicit val system: ActorSystem[Nothing] = context.system
    val user = context.spawn(userActor.behavior(), "UserServiceActor")
    context.watch(context.spawn(userActor.behavior(), "UserServiceActor"))
    val venue = context.spawn(venueActor.behavior(), "VenueActor")
    context.watch(venue)
    val purchase = context.spawn(purchaseActor.behavior(), "PurchaseActor")
    context.watch(purchase)

    val userRoute = UserRouteConfig.config(user)
    val venueRoute = VenueRouteConfig.config(venue, purchase)
    userRoute.route ~ venueRoute.route
  }
}
