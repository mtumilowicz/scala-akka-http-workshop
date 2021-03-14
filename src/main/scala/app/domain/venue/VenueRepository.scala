package app.domain.venue

import app.domain.error.DomainError

trait VenueRepository {

  def findAll: Venues

  def findById(id: VenueId): Either[DomainError, Venue]

  def save(venue: Venue): Venue

  def deleteById(id: VenueId): Option[VenueId]
}
