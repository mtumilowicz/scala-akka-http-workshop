package app.gateway.venue.in

import app.domain.purchase.BuyerId


case class BuyerIdApiInput(playerId: String) {
  def toDomain: BuyerId =
    BuyerId(playerId)
}
