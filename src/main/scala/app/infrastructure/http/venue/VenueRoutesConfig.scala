package app.infrastructure.http.venue

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ActorRef, ActorSystem}
import app.gateway.venue.VenueRoutes
import app.infrastructure.actor.{PurchaseActor, VenueActor}
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}

object VenueRoutesConfig {

  def config(venueActor: ActorRef[VenueActor.VenueCommand],
             purchaseActor: ActorRef[PurchaseActor.PurchaseCommand])
            (implicit system: ActorSystem[_]): VenueRoutes =
    new VenueRoutes(venueActor, purchaseActor)
}
