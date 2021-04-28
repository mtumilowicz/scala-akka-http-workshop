package app.domain.venue

import cats.data.{EitherT, OptionT}
import app.domain.error.DomainError

import scala.concurrent.Future

trait VenueRepository {

  def findAll: Future[Venues]

  def findById(id: VenueId): EitherT[Future, DomainError, Venue]

  def save(venue: Venue): EitherT[Future, DomainError, Venue]

  def deleteById(id: VenueId): OptionT[Future, VenueId]
}
