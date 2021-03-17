package app.domain.purchase

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
    pay(newOwner, venue)
      .flatMap(_ => venueService.changeOwner(venue.id, newOwner))
      .left
      .map {
        case CantAffordTransactionError(userId) => CantAffordBuyingVenue(userId, venue)
        case error => error
      }
  }

  private def pay(payer: UserId, venue: Venue) =
    venue.owner match {
      case Some(owner) => userService.transfer(owner, payer, venue.price)
      case None => userService.postOutgoingAmount(payer, venue.price)
    }

  private def checkIfOwnerDifferentThanBuyer(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] = {
    val userId = purchase.buyer.toUserId
    Either.cond(!venue.owner.contains(userId), venue, CantBuyFromYourself(userId))
  }

}
