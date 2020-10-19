package app.gateway.in

import app.domain.NewUserCommand

case class NewUserApiInput(name: String, age: Int, countryOfResidence: String) {
  def toDomain: NewUserCommand = NewUserCommand(name, age, countryOfResidence)
}