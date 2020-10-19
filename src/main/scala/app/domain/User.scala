package app.domain

final case class User(name: String, age: Int, countryOfResidence: String)

object User {
  def createFrom(newUserCommand: NewUserCommand): User =
    User(newUserCommand.name, newUserCommand.age, newUserCommand.countryOfResidence)
}