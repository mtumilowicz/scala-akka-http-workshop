package app.domain.purchase

import app.domain.user.UserId
import app.domain.venue.VenueId

case class NewPurchase(buyer: UserId, venue: VenueId)
