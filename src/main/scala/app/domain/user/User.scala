package app.domain.user

import app.domain.cash.NonNegativeAmount

final case class User(id: UserId, name: String, budget: NonNegativeAmount)

object User {
  def createFrom(userId: UserId, input: NewUserInput): User =
    User(userId, input.name, input.budget)

  def createFrom(input: ReplaceUserInput): User =
    User(input.id, input.name, input.price)
}