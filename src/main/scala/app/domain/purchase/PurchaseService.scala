package app.domain.purchase

import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError
import app.domain.purchase.error.{CantAffordBuyingVenue, CantBuyFromYourself}
import app.domain.user.error.CantAffordTransactionError
import app.domain.user.{UserId, UserService}
import app.domain.venue.{Venue, VenueService}

class PurchaseService(
                       val userService: UserService,
                       val venueService: VenueService
                     ) {

  def purchase(purchase: NewPurchase): Either[DomainError, Venue] =
    venueService.findById(purchase.venue)
      .flatMap(venue => checkIfOwnerDifferentThanBuyer(purchase, venue))
      .flatMap(venue => proceed(purchase, venue))

  private def proceed(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] = {
    val newOwner = purchase.buyer.toUserId
    userService.postOutgoingAmount(newOwner, venue.price)
      .flatMap(_ => payPreviousOwnerOf(venue))
      .flatMap(_ => venueService.changeOwner(venue.id, newOwner))
      .left
      .map {
        case CantAffordTransactionError(userId) => CantAffordBuyingVenue(userId, venue)
        case error => error
      }
  }

  private def payPreviousOwnerOf(venue: Venue): Either[DomainError, Option[UserId]] =
    venue.owner match {
      case Some(previousOwner) => makePayTo(previousOwner, venue.price).map(Some(_))
      case None => Right(None)
    }

  private def makePayTo(recipient: UserId, price: NonNegativeAmount): Either[DomainError, UserId] =
    userService.postIncomingAmount(recipient, price)

  private def checkIfOwnerDifferentThanBuyer(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] = {
    val userId = purchase.buyer.toUserId
    Either.cond(!venue.owner.contains(userId), venue, CantBuyFromYourself(userId))
  }

}
