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

  def purchase(purchase: NewPurchase): Either[DomainError, Venue] = venueService.findById(purchase.venue)
    .flatMap(venue => checkIfOwnerDifferentThanBuyer(purchase, venue))
    .flatMap(venue => proceed(purchase, venue))

  private def proceed(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] =
    userService.postOutgoingAmount(purchase.buyer.toUserId, venue.price)
      .map(newOwner => {
        payPreviousOwnerOf(venue)
        venue.assignOwner(newOwner)
      })
      .map(venueService.save)
      .left
      .map {
        case CantAffordTransactionError(userId) => CantAffordBuyingVenue(userId, venue)
        case error => error
      }

  private def payPreviousOwnerOf(venue: Venue): Option[Either[DomainError, UserId]] =
    venue.owner.map(previousOwner => pay(previousOwner, venue.price))

  private def pay(previousOwner: UserId, price: NonNegativeAmount): Either[DomainError, UserId] =
    userService.postIncomingAmount(previousOwner, price)

  private def checkIfOwnerDifferentThanBuyer(purchase: NewPurchase, venue: Venue): Either[DomainError, Venue] = {
    val userId = purchase.buyer.toUserId
    Either.cond(!venue.owner.contains(userId), venue, CantBuyFromYourself(userId))
  }

}
