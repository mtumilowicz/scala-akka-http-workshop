package app.domain.purchase

import app.domain.user.UserId

case class BuyerId(raw: String) {
  def toUserId: UserId =
    UserId(raw)
}
