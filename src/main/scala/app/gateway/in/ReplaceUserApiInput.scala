package app.gateway.in

import app.domain.{ReplaceUserInput, UserId}

case class ReplaceUserApiInput(name: String, age: Int) {
  def toDomain(id: String): ReplaceUserInput =
    ReplaceUserInput(UserId(id), name, age)
}
