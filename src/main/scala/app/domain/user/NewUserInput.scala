package app.domain.user

import app.domain.cash.NonNegativeAmount

case class NewUserInput(name: String, budget: NonNegativeAmount)
