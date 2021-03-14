package app.gateway.venue.out

import app.domain.venue.{Venue, Venues}

case class VenueApiOutput(
                           id: String,
                           name: String,
                           price: Int,
                           owner: Option[String]
                         ) {

}

object VenueApiOutputBuilder {
  def fromDomain(venues: Venues): List[VenueApiOutput] =
    venues.raw.map(fromDomain)

  def fromDomain(venue: Venue): VenueApiOutput =
    VenueApiOutput(
      id = venue.id.asString(),
      name = venue.name,
      price = venue.price.raw,
      owner = venue.owner.map(_.raw)
    )
}