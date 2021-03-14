package app.gateway.venue.in

import app.domain.cash.NonNegativeAmount
import app.domain.venue.{Venue, VenueId}


case class NewVenueApiInput(
                             name: String,
                             price: Int
                           ) {
  def toDomain(venueId: VenueId): Venue =
    Venue(id = venueId,
      price = NonNegativeAmount(price),
      name = name
    )
}