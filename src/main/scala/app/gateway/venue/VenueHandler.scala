package app.gateway.venue

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.error.DomainError
import app.domain.purchase.BuyerId
import app.domain.venue.VenueId
import app.gateway.venue.in.NewVenueApiInput
import app.gateway.venue.out.{VenueApiOutput, VenueApiOutputBuilder}
import app.infrastructure.actor.PurchaseActor.Purchase
import app.infrastructure.actor.VenueActor.{CreateOrReplaceVenue, DeleteVenueById, GetVenueById, GetVenues}
import app.infrastructure.actor.{PurchaseActor, VenueActor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueHandler(venueActor: ActorRef[VenueActor.VenueCommand],
                   purchaseActor: ActorRef[PurchaseActor.PurchaseCommand])
                  (implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getVenues: Future[List[VenueApiOutput]] =
    venueActor.ask(GetVenues).map(VenueApiOutputBuilder.fromDomain)

  def getVenue(id: VenueId): Future[Either[String, VenueApiOutput]] =
    venueActor.ask(GetVenueById(id, _))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def createVenue(venueId: VenueId, newVenueApiInput: NewVenueApiInput): Future[VenueApiOutput] =
    venueActor.ask(CreateOrReplaceVenue(newVenueApiInput.toDomain(venueId), _))
      .map(VenueApiOutputBuilder.fromDomain)

  def purchase(buyerId: BuyerId, venueId: VenueId): Future[Either[String, VenueApiOutput]] =
    purchaseActor.ask(Purchase(buyerId, venueId, _))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def domainErrorAsString[B](either: Either[DomainError, B]): Either[String, B] =
    either.left.map(_.message())

  def deleteVenue(id: VenueId): Future[Option[String]] =
    venueActor.ask(DeleteVenueById(id, _))
      .map(_.map(_.asString()))
}
