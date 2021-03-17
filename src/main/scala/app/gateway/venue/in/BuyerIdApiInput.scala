package app.gateway.venue.in

import app.domain.purchase.BuyerId


case class BuyerIdApiInput(id: String) {
  def toDomain: BuyerId =
    BuyerId(id)
}
