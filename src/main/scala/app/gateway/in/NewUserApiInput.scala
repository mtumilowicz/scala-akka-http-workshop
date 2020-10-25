package app.gateway.in

import app.domain.NewUserInput

case class NewUserApiInput(name: String, age: Int) {
  def toDomain: NewUserInput = NewUserInput(name, age)
}