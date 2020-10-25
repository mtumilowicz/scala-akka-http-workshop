package app.domain

final case class User(id: UserId, name: String, age: Int)

object User {
  def createFrom(userId: UserId, input: NewUserInput): User =
    User(userId, input.name, input.age)

  def createFrom(input: ReplaceUserInput): User =
    User(input.id, input.name, input.age)
}