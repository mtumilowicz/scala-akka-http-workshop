package app.domain.venue

import java.util.UUID

case class VenueId(raw: UUID) {
  def asString(): String =
    raw.toString
}
