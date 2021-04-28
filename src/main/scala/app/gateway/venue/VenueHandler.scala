package app.gateway.venue

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import app.domain.error.DomainError
import app.domain.purchase.{BuyerId, NewPurchase, PurchaseService}
import app.domain.venue.{VenueId, VenueService}
import app.gateway.venue.in.NewVenueApiInput
import app.gateway.venue.out.{VenueApiOutput, VenueApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueHandler(venueService: VenueService,
                   purchaseService: PurchaseService)
                  (implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getVenues: Future[List[VenueApiOutput]] =
    Future(venueService.findAll).map(VenueApiOutputBuilder.fromDomain)

  def getVenue(id: VenueId): Future[Either[String, VenueApiOutput]] =
    Future(venueService.findById(id))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def createVenue(venueId: VenueId, newVenueApiInput: NewVenueApiInput): Future[VenueApiOutput] =
    Future(venueService.save(newVenueApiInput.toDomain(venueId)))
      .map(VenueApiOutputBuilder.fromDomain)

  def purchase(buyerId: BuyerId, venueId: VenueId): Future[Either[String, VenueApiOutput]] =
    Future(purchaseService.purchase(NewPurchase(buyerId, venueId)))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def deleteVenue(id: VenueId): Future[Option[String]] =
    Future(venueService.deleteById(id))
      .map(_.map(_.asString()))

  def domainErrorAsString[B](either: Either[DomainError, B]): Either[String, B] =
    either.left.map(_.message())
}
