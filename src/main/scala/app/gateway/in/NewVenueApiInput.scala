package app.gateway.in

import cats.data.ValidatedNec
import cats.implicits._
import app.domain.cash.NonNegativeAmount
import app.domain.error.DomainError
import app.domain.venue.{Venue, VenueId}

private[gateway] case class NewVenueApiInput(
                                              name: String,
                                              price: Int
                                            ) {
  def toDomain(venueId: VenueId): ValidatedNec[DomainError, Venue] = {
    NonNegativeAmount(price)
      .map((amount: NonNegativeAmount) =>
        Venue(id = venueId,
          price = amount,
          name = name
        )
      ).toValidatedNec
  }
}