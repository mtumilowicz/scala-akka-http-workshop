package app.domain.purchase

import app.domain.venue.VenueId

case class NewPurchase(buyer: BuyerId, venue: VenueId)
