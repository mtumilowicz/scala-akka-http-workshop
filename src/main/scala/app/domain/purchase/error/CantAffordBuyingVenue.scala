package app.domain.purchase.error

import app.domain.error.DomainError
import app.domain.user.UserId
import app.domain.venue.Venue

case class CantAffordBuyingVenue(userId: UserId, venue: Venue) extends DomainError {
  override def message(): String =
    s"${userId.raw} can't afford ${venue.name}"
}
