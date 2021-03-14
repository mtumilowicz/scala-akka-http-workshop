package app.domain.venue

import app.domain.error.DomainError

class VenueService(val repository: VenueRepository) {

  def findAll: Venues =
    repository.findAll

  def findById(id: VenueId): Either[DomainError, Venue] =
    repository.findById(id)

  def save(venue: Venue): Venue =
    repository.save(venue)

  def deleteById(id: VenueId): Option[VenueId] =
    repository.deleteById(id)

}
