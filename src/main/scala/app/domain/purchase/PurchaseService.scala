package app.domain.purchase

import app.domain.error.DomainError
import app.domain.purchase.error.{CantAffordBuyingVenueError, CantBuyFromYourselfError}
import app.domain.user.error.CantAffordTransactionError
import app.domain.user.{UserBalanceService, UserId}
import app.domain.venue.{Venue, VenueService}
import cats.data._
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PurchaseService(
                       val userBalanceService: UserBalanceService,
                       val venueService: VenueService
                     ) {

  def purchase(purchase: NewPurchase): EitherT[Future, DomainError, Venue] =
    for {
      venue <- venueService.findById(purchase.venue)
      verifiedVenue <- checkIfOwnerDifferentThanBuyer(purchase, venue).toEitherT[Future]
      purchasedVenue <- proceed(purchase, verifiedVenue)
    } yield purchasedVenue

  private def proceed(purchase: NewPurchase, venue: Venue): EitherT[Future, DomainError, Venue] = {
    val newOwner = purchase.buyer
    pay(newOwner, venue)
      .flatMap(_ => venueService.changeOwner(venue.id, newOwner))
      .leftMap {
        case CantAffordTransactionError(userId) => CantAffordBuyingVenueError(userId, venue)
        case error => error
      }
  }

  private def pay(payer: UserId, venue: Venue): EitherT[Future, DomainError, UserId] =
    venue.owner match {
      case Some(owner) => userBalanceService.transfer(owner, payer, venue.price)
      case None => userBalanceService.postOutgoingAmount(payer, venue.price)
    }

  private def checkIfOwnerDifferentThanBuyer(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] = {
    val userId = purchase.buyer
    Either.cond(!venue.owner.contains(userId), venue, CantBuyFromYourselfError(userId))
  }

}
