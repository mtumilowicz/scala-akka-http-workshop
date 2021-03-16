package app.domain.venue

import app.domain.error.DomainError
import app.domain.user.UserId

class VenueService(val repository: VenueRepository) {

  def findAll: Venues =
    repository.findAll

  def findById(id: VenueId): Either[DomainError, Venue] =
    repository.findById(id)

  def changeOwner(venueId: VenueId, newOwner: UserId): Either[DomainError, Venue] =
    findById(venueId)
      .map(_.assignOwner(newOwner))
      .map(save)

  def save(venue: Venue): Venue =
    repository.save(venue)

  def deleteById(id: VenueId): Option[VenueId] =
    repository.deleteById(id)

}
