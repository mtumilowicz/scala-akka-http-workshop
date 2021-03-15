package app.infrastructure.http.venue

import akka.actor.typed.{ActorRef, ActorSystem}
import app.gateway.venue.VenueRoute
import app.infrastructure.actor.{PurchaseActor, VenueActor}

object VenueRouteConfig {

  def config(venueActor: ActorRef[VenueActor.VenueCommand],
             purchaseActor: ActorRef[PurchaseActor.PurchaseCommand])
            (implicit system: ActorSystem[_]): VenueRoute =
    new VenueRoute(venueActor, purchaseActor)
}
