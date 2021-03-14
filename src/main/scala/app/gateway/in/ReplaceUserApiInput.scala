package app.gateway.in

import app.domain.cash.NonNegativeAmount
import app.domain._
import app.domain.user.{ReplaceUserInput, UserId}

case class ReplaceUserApiInput(name: String, budget: Int) {
  def toDomain(id: String): ReplaceUserInput =
    user.ReplaceUserInput(UserId(id), name, NonNegativeAmount(budget))
}
