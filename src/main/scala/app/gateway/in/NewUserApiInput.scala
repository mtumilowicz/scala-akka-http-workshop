package app.gateway.in

import app.domain.NewUserInput

case class NewUserApiInput(name: String, age: Int, countryOfResidence: String) {
  def toDomain: NewUserInput = NewUserInput(name, age, countryOfResidence)
}