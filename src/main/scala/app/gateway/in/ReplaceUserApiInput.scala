package app.gateway.in

import app.domain.{ReplaceUserInput, UserId}

case class ReplaceUserApiInput(name: String, age: Int, countryOfResidence: String) {
  def toDomain(id: String): ReplaceUserInput =
    ReplaceUserInput(UserId(id), name, age, countryOfResidence)
}
