package app.gateway.venue

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import app.domain.error.DomainError
import app.domain.purchase.BuyerId
import app.domain.venue.VenueId
import app.gateway.venue.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.venue.out.{VenueApiOutput, VenueApiOutputBuilder}
import app.infrastructure.actor.PurchaseActor.Purchase
import app.infrastructure.actor.VenueActor._
import app.infrastructure.actor.{PurchaseActor, VenueActor}
import app.infrastructure.http.venue.VenueJsonFormats._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueRoutes(venueActor: ActorRef[VenueActor.VenueCommand],
                  purchaseActor: ActorRef[PurchaseActor.PurchaseCommand])(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val route: Route = pathPrefix("venues") {
    concat(
      pathEnd {
        get {
          complete(getVenues)
        }
      },
      path(JavaUUID) { id =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(getVenue(VenueId(id))) {
                case Right(value) => complete(value)
                case Left(error) => complete(StatusCodes.NotFound, error)
              }
            }
          },
          put {
            entity(as[NewVenueApiInput]) { venue =>
              onSuccess(createVenue(VenueId(id), venue)) { venue => {
                complete(StatusCodes.OK, venue.id)
              }
              }
            }
          },
          delete {
            onSuccess(deleteVenue(VenueId(id))) {
              case Some(value) => complete(value)
              case None => complete(StatusCodes.NotFound, s"Venue with given id: $id does not exist.")
            }
          },
        )
      },
      path(JavaUUID / "buy") { id =>
        post {
          entity(as[BuyerIdApiInput]) { buyerId =>
            onSuccess(purchase(buyerId.toDomain, VenueId(id))) {
              case Left(error) => complete(StatusCodes.BadRequest, error)
              case Right(value) => complete(StatusCodes.OK,
                s"${value.name} was bought by ${value.owner.orNull} for ${value.price}")
            }
          }
        }
      }
    )
  }

  def getVenues: Future[List[VenueApiOutput]] =
    venueActor.ask(GetVenues).map(VenueApiOutputBuilder.fromDomain)

  def getVenue(id: VenueId): Future[Either[String, VenueApiOutput]] =
    venueActor.ask(GetVenueById(id, _))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def domainErrorAsString[B](either: Either[DomainError, B]): Either[String, B] =
    either.left.map(_.message())

  def createVenue(venueId: VenueId, newVenueApiInput: NewVenueApiInput): Future[VenueApiOutput] =
    venueActor.ask(CreateOrReplaceVenue(newVenueApiInput.toDomain(venueId), _))
      .map(VenueApiOutputBuilder.fromDomain)

  def purchase(buyerId: BuyerId, venueId: VenueId): Future[Either[String, VenueApiOutput]] =
    purchaseActor.ask(Purchase(buyerId, venueId, _))
      .map(_.map(VenueApiOutputBuilder.fromDomain))
      .map(domainErrorAsString)

  def deleteVenue(id: VenueId): Future[Option[String]] =
    venueActor.ask(DeleteVenueById(id, _))
      .map(_.map(_.asString()))
}
