package answers.app.gateway.in

import answers.app.domain.NewUserInput

case class NewUserApiInput(name: String, age: Int) {
  def toDomain: NewUserInput = NewUserInput(name, age)
}