package app.gateway.user.in

import app.domain.cash.NonNegativeAmount
import app.domain.user
import app.domain.user.NewUserInput

case class NewUserApiInput(name: String, budget: Int) {
  def toDomain: NewUserInput = user.NewUserInput(name, NonNegativeAmount(budget))
}