package app.infrastructure.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import app.domain.purchase.PurchaseService
import app.domain.user.UserService
import app.domain.venue.VenueService
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}
import app.infrastructure.http.user.UserRouteConfig
import app.infrastructure.http.venue.VenueRouteConfig

object RouteConfig {

  def inMemoryRoute(implicit context: ActorContext[Nothing]): Route = {
    implicit val system = context.system
    val userService = UserConfig.inMemoryService()
    val venueService = VenueConfig.inMemoryService()
    val purchaseService = PurchaseConfig.service(userService, venueService)

    route(userService,
      venueService,
      purchaseService
    )
  }

  private def route(userService: UserService,
                    venueService: VenueService,
                    purchaseService: PurchaseService)
                   (implicit context: ActorContext[Nothing]): Route = {
    implicit val system: ActorSystem[Nothing] = context.system

    val userRoute = UserRouteConfig.config(userService)
    val venueRoute = VenueRouteConfig.config(venueService, purchaseService)
    userRoute.route ~ venueRoute.route
  }
}
