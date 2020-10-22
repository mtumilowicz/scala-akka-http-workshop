package app.gateway.in

import app.domain.{ReplaceUserInput, UserId}

case class ReplaceUserApiInput(id: String, name: String, age: Int, countryOfResidence: String) {
  def toDomain: ReplaceUserInput = ReplaceUserInput(UserId(id), name, age, countryOfResidence)
}
