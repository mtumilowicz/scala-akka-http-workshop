package app.infrastructure.http

import akka.actor.typed.scaladsl.ActorContext
import app.gateway.VenueRoutes
import app.infrastructure.config.{PurchaseConfig, UserBalanceConfig, VenueConfig}

object RoutesConfig {

  def config(context: ActorContext[Nothing]): VenueRoutes = {
    val venueService = VenueConfig.inMemoryService()
    val purchaseService = PurchaseConfig.service(UserBalanceConfig.inMemoryService(), venueService)

    new VenueRoutes(venueService, purchaseService)(context.system)
  }
}
