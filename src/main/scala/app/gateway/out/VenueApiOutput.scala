package app.gateway.out

import app.domain.venue.{Venue, Venues}

private[gateway] case class VenueApiOutput(
                                            id: String,
                                            name: String,
                                            price: BigInt,
                                            owner: Option[String]
                                          ) {

}

object VenueApiOutputBuilder {
  def fromDomain(venues: Venues): List[VenueApiOutput] =
    venues.raw.map(fromDomain)

  def fromDomain(venue: Venue): VenueApiOutput =
    VenueApiOutput(
      id = venue.id.raw,
      name = venue.name,
      price = venue.price.raw,
      owner = venue.owner.map(_.raw)
    )
}