package app.infrastructure.actor

import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import app.domain.error.DomainError
import app.domain.venue.{Venue, VenueId, VenueService, Venues}
import app.infrastructure.actor.VenueActor._

class VenueActor(val venueService: VenueService) {

  def behavior(): Behavior[VenueCommand] =
    Behaviors.receiveMessage {
      case GetVenues(replyTo) =>
        replyTo ! venueService.findAll
        Behaviors.same
      case CreateOrReplaceVenue(venue, replyTo) =>
        replyTo ! venueService.save(venue)
        Behaviors.same
      case GetVenueById(id, replyTo) =>
        replyTo ! venueService.findById(id)
        Behaviors.same
      case DeleteVenueById(id, replyTo) =>
        replyTo ! venueService.deleteById(id)
        Behaviors.same
    }
}

object VenueActor {

  sealed trait VenueCommand

  final case class GetVenues(replyTo: ActorRef[Venues]) extends VenueCommand

  final case class CreateOrReplaceVenue(venue: Venue, replyTo: ActorRef[Venue]) extends VenueCommand

  final case class GetVenueById(id: VenueId, replyTo: ActorRef[Either[DomainError, Venue]]) extends VenueCommand

  final case class DeleteVenueById(id: VenueId, replyTo: ActorRef[Option[VenueId]]) extends VenueCommand

}
