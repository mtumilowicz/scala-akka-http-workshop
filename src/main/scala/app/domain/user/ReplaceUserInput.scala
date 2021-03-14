package app.domain.user

import app.domain.cash.NonNegativeAmount

case class ReplaceUserInput(id: UserId, name: String, price: NonNegativeAmount)
