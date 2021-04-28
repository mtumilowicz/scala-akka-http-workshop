package app.infrastructure.config

import app.domain.venue.{VenueRepository, VenueService}
import app.infrastructure.repository.VenueInMemoryRepository

object VenueConfig {

  def inMemoryService(): VenueService =
    new VenueService(inMemoryRepository())

  def inMemoryRepository(): VenueRepository =
    new VenueInMemoryRepository()

}