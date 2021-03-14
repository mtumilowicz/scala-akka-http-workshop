package app.domain.venue

import app.domain.cash.NonNegativeAmount
import app.domain.user.UserId

case class Venue(
                  id: VenueId,
                  price: NonNegativeAmount,
                  name: String,
                  owner: Option[UserId] = Option.empty
                ) {

  def hasOwner: Boolean =
    owner.isDefined

  def assignOwner(newOwner: UserId): Venue =
    Venue(
      id = id,
      price = price,
      name = name,
      owner = Some(newOwner))
}
