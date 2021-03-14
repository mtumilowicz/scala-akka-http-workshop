package app.domain.purchase

import app.domain.user.UserId

case class BuyerId(playerId: String) {
  def toUserId: UserId =
    UserId(playerId)
}
