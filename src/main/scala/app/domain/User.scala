package app.domain

final case class User(id: UserId, name: String, age: Int, countryOfResidence: String)

object User {
  def createFrom(input: NewUserInput): User =
    User(UserId(java.util.UUID.randomUUID().toString), input.name, input.age, input.countryOfResidence)
}