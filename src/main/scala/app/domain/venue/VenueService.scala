package app.domain.venue

import cats.data._
import cats.implicits._
import app.domain.error.DomainError
import app.domain.user.UserId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueService(val repository: VenueRepository) {

  def findAll: Future[Venues] =
    repository.findAll

  def changeOwner(venueId: VenueId, newOwner: UserId): EitherT[Future, DomainError, Venue] = {
    findById(venueId)
      .map(_.assignOwner(newOwner))
      .flatMap(save)
  }

  def findById(id: VenueId): EitherT[Future, DomainError, Venue] =
    repository.findById(id)

  def save(venue: Venue): EitherT[Future, DomainError, Venue] =
    repository.save(venue)

  def deleteById(id: VenueId): OptionT[Future, VenueId] =
    repository.deleteById(id)

}
