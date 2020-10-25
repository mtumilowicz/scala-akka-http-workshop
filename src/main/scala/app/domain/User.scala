package app.domain

final case class User(id: UserId, name: String, age: Int)

object User {
  def createFrom(input: NewUserInput): User =
    User(UserId(java.util.UUID.randomUUID().toString), input.name, input.age)

  def createFrom(input: ReplaceUserInput): User =
    User(input.id, input.name, input.age)
}