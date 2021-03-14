package app.infrastructure.repository

import app.domain.error.DomainError
import app.domain.venue.error.VenueNotFoundError
import app.domain.venue.{Venue, VenueId, VenueRepository, Venues}

import scala.collection.mutable

class VenueInMemoryRepository extends VenueRepository {
  val map: mutable.Map[VenueId, Venue] = mutable.Map()

  override def findAll: Venues =
    Venues(map.values.toList)

  override def findById(id: VenueId): Either[DomainError, Venue] =
    map.get(id).toRight(VenueNotFoundError(id))

  override def save(venue: Venue): Venue = {
    map.put(venue.id, venue)
    venue
  }

  override def deleteById(id: VenueId): Option[VenueId] =
    map.remove(id).map(_.id)
}
