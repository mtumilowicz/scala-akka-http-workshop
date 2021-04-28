package app.infrastructure.http.venue

import akka.actor.typed.{ActorRef, ActorSystem}
import app.domain.purchase.PurchaseService
import app.domain.venue.VenueService
import app.gateway.venue.{VenueHandler, VenueRoute}

object VenueRouteConfig {

  def config(venueService: VenueService,
             purchaseService: PurchaseService)
            (implicit system: ActorSystem[_]): VenueRoute =
    new VenueRoute(new VenueHandler(venueService, purchaseService))
}
