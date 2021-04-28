package app.domain.purchase.error

import app.domain.error.DomainError
import app.domain.user.UserId
import app.domain.venue.Venue

case class CantAffordBuyingVenueError(userId: UserId, venue: Venue) extends DomainError
