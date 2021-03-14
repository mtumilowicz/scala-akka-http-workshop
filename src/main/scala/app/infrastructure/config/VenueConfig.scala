package app.infrastructure.config

import app.domain.venue.{VenueRepository, VenueService}
import app.infrastructure.actor.VenueActor
import app.infrastructure.repository.VenueInMemoryRepository

object VenueConfig {

  def inMemoryService(): VenueService =
    new VenueService(inMemoryRepository())

  def inMemoryRepository(): VenueRepository =
    new VenueInMemoryRepository()

  def actor(venueService: VenueService): VenueActor =
    new VenueActor(venueService)
}
