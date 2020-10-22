package app.infrastructure

import app.domain.UserId
import app.gateway.in.NewUserApiInput
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val newUserApiInputJsonFormat = jsonFormat3(NewUserApiInput)
  implicit val userOutputJsonFormat = jsonFormat3(UserApiOutput(_, _, _))
  implicit val usersOutputJsonFormat = jsonFormat1(UsersApiOutput(_))

  implicit val actionPerformedJsonFormat = jsonFormat1(UserId)
}
