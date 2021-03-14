package app.infrastructure.http

import akka.actor.typed.scaladsl.ActorContext
import app.gateway.venue.VenueRoutes
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}

object VenueRoutesConfig {

  def config(context: ActorContext[Nothing]): VenueRoutes = {
    val venueService = VenueConfig.inMemoryService()
    val purchaseConfig = PurchaseConfig.service(UserConfig.inMemoryService, venueService)
    val venueActor = context.spawn(VenueConfig.actor(venueService).behavior(), "VenueActor")
    context.watch(venueActor)
    val purchaseActor = context.spawn(PurchaseConfig.actor(purchaseConfig).behavior(), "PurchaseActor")
    context.watch(purchaseActor)

    new VenueRoutes(venueActor, purchaseActor)(context.system)
  }
}
