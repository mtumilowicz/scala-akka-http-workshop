package app.domain

final case class User(name: String, age: Int, countryOfResidence: String)

object User {
  def createFrom(input: NewUserInput): User =
    User(input.name, input.age, input.countryOfResidence)
}