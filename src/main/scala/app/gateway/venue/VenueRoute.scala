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
import app.infrastructure.http.JsonFormats._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueRoute(venueHandler: VenueHandler)(implicit val system: ActorSystem[_]) {

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val route: Route = pathPrefix("venues") {
    concat(
      pathEnd {
        get {
          complete(venueHandler.getVenues)
        }
      },
      path(JavaUUID) { id =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(venueHandler.getVenue(VenueId(id))) {
                case Right(value) => complete(value)
                case Left(error) => complete(StatusCodes.NotFound, error)
              }
            }
          },
          put {
            entity(as[NewVenueApiInput]) { venue =>
              onSuccess(venueHandler.createVenue(VenueId(id), venue)) { venue => {
                complete(StatusCodes.OK, venue.id)
              }
              }
            }
          },
          delete {
            onSuccess(venueHandler.deleteVenue(VenueId(id))) {
              case Some(value) => complete(value)
              case None => complete(StatusCodes.NotFound, s"Venue with given id: $id does not exist.")
            }
          },
        )
      },
      path(JavaUUID / "buy") { id =>
        post {
          entity(as[BuyerIdApiInput]) { buyerId =>
            onSuccess(venueHandler.purchase(buyerId.toDomain, VenueId(id))) {
              case Left(error) => complete(StatusCodes.BadRequest, error)
              case Right(value) => complete(StatusCodes.OK,
                s"${value.name} was bought by ${value.owner.orNull} for ${value.price}")
            }
          }
        }
      }
    )
  }
}
