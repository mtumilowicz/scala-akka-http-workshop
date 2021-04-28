package app.gateway.in

import app.domain.user.UserId

private[gateway] case class BuyerIdApiInput(playerId: String) {
  def toDomain: UserId =
    UserId(playerId)
}
