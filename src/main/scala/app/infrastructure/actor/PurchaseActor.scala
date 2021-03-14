package app.infrastructure.actor

import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.domain.error.DomainError
import app.domain.purchase.{BuyerId, NewPurchase, PurchaseService}
import app.domain.venue.{Venue, VenueId}
import app.infrastructure.actor.PurchaseActor.{Purchase, PurchaseCommand}

class PurchaseActor(purchaseService: PurchaseService) {
  def behavior(): Behavior[PurchaseCommand] =
    Behaviors.receiveMessage {
      case Purchase(userId, venueId, replyTo) =>
        replyTo ! purchaseService.purchase(NewPurchase(userId, venueId))
        Behaviors.same
    }
}

object PurchaseActor {

  sealed trait PurchaseCommand

  final case class Purchase(buyerId: BuyerId,
                            venueId: VenueId,
                            replyTo: ActorRef[Either[DomainError, Venue]]
                           ) extends PurchaseCommand

}
