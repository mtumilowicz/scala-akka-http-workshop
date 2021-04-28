package app.gateway

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import cats.data.{EitherT, NonEmptyChain, OptionT, Validated}
import cats.implicits._
import app.domain.error.DomainError
import app.domain.purchase.error.{CantAffordBuyingVenueError, CantBuyFromYourselfError}
import app.domain.purchase.{NewPurchase, PurchaseService}
import app.domain.user.UserId
import app.domain.user.error.{CantAffordTransactionError, UserNotFoundError}
import app.domain.venue.error.VenueNotFoundError
import app.domain.venue.{Venue, VenueId, VenueService}
import app.gateway.JsonFormats._
import app.gateway.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.out.{VenueApiOutput, VenueApiOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueRoutes(venueService: VenueService,
                  purchaseService: PurchaseService)(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val routes: Route = pathPrefix("venues") {
    concat(
      pathEnd {
        getVenuesRoute
      },
      path(Segment) { id =>
        concat(
          getVenueRoute(id),
          putVenueRoute(id),
          deleteVenueRoute(id)
        )
      },
      path(Segment / "buy") { id =>
        postBuyVenueRoute(id)
      }
    )
  }

  private def getVenuesRoute: Route =
    get {
      complete(getVenues)
    }

  private def getVenues: Future[List[VenueApiOutput]] =
    venueService.findAll
      .map(VenueApiOutputBuilder.fromDomain)

  private def getVenueRoute(id: String): Route =
    get {
      rejectEmptyResponse {
        onSuccess(getVenue(VenueId(id)).value) {
          case Right(value) => complete(value)
          case Left(error) => complete(StatusCodes.NotFound, error)
        }
      }
    }

  private def getVenue(id: VenueId): EitherT[Future, String, VenueApiOutput] =
    venueService.findById(id)
      .map(VenueApiOutputBuilder.fromDomain)
      .leftMap(x => domainErrorAsString(x))

  private def domainErrorAsString[B](error: DomainError): String =
    error match {
      case CantAffordBuyingVenueError(userId, venue) => s"${userId.raw} can't afford ${venue.name}"
      case CantBuyFromYourselfError(userId) => s"User ${userId.raw} cannot buy from himself."
      case UserNotFoundError(userId) => s"${userId.raw} not found."
      case VenueNotFoundError(venueId) => s"${venueId.raw} not found."
      case CantAffordTransactionError(userId) => s"${userId.raw} can't afford that transaction."
      case _ => "error occurred"
    }

  private def putVenueRoute(id: String): Route =
    put {
      entity(as[NewVenueApiInput]) { input =>
        onSuccess(createVenue(VenueId(id), input).value) {
          case Left(errors) => complete(StatusCodes.BadRequest, errors.toList)
          case Right(venue) => complete(StatusCodes.OK, venue.id.raw)
        }
      }
    }

  private def createVenue(venueId: VenueId, newVenueApiInput: NewVenueApiInput): EitherT[Future, NonEmptyChain[String], Venue] =
    newVenueApiInput.toDomain(venueId) match {
      case Validated.Valid(a) => venueService.save(a).leftMap(domainErrorAsString).leftMap(NonEmptyChain(_))
      case Validated.Invalid(e) => Left(e).leftMap(_.map(domainErrorAsString)).toEitherT
    }

  private def deleteVenueRoute(id: String): Route =
    delete {
      onSuccess(deleteVenue(VenueId(id)).value) {
        case Some(value) => complete(value)
        case None => complete(StatusCodes.NotFound, s"Venue with given id: $id does not exist.")
      }
    }

  private def deleteVenue(id: VenueId): OptionT[Future, String] =
    venueService.deleteById(id)
      .map(_.raw)

  private def postBuyVenueRoute(id: String): Route =
    post {
      entity(as[BuyerIdApiInput]) { buyerId =>
        onSuccess(purchase(buyerId.toDomain, VenueId(id)).value) {
          case Left(error) => complete(StatusCodes.BadRequest, error)
          case Right(value) => complete(StatusCodes.OK,
            s"${value.name} was bought by ${value.owner.orNull} for ${value.price}")
        }
      }
    }

  private def purchase(buyerId: UserId, venueId: VenueId): EitherT[Future, String, VenueApiOutput] =
    purchaseService.purchase(NewPurchase(buyerId, venueId))
      .map(x => VenueApiOutputBuilder.fromDomain(x))
      .leftMap(x => domainErrorAsString(x))
}
