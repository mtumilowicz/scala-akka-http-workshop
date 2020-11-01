package answers.app.gateway.in

import answers.app.domain.{ReplaceUserInput, UserId}

case class ReplaceUserApiInput(name: String, age: Int) {
  def toDomain(id: String): ReplaceUserInput =
    ReplaceUserInput(UserId(id), name, age)
}
