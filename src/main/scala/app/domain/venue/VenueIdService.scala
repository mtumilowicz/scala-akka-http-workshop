package app.domain.venue

import java.util.UUID

class VenueIdService {
  def generate(): VenueId =
    VenueId(UUID.randomUUID().toString)
}
