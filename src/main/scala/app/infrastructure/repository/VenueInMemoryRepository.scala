package app.infrastructure.repository

import cats.data.{EitherT, OptionT}
import cats.implicits._
import app.domain.error.DomainError
import app.domain.venue.error.VenueNotFoundError
import app.domain.venue.{Venue, VenueId, VenueRepository, Venues}

import scala.collection.concurrent.{Map, TrieMap}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VenueInMemoryRepository extends VenueRepository {
  val map: Map[VenueId, Venue] = TrieMap()

  override def findAll: Future[Venues] =
    Future(Venues(map.values.toList))

  override def findById(id: VenueId): EitherT[Future, DomainError, Venue] =
    map.get(id).toRight[DomainError](VenueNotFoundError(id)).toEitherT

  override def save(venue: Venue): EitherT[Future, DomainError, Venue] = {
    map.put(venue.id, venue)
    Right(venue).toEitherT
  }

  override def deleteById(id: VenueId): OptionT[Future, VenueId] =
    map.remove(id).map(_.id).toOptionT
}
