package app.infrastructure

import app.domain.UserId
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  import DefaultJsonProtocol._

  implicit val newUserApiInputJsonFormat = jsonFormat3(NewUserApiInput)
  implicit val userOutputJsonFormat = jsonFormat3(UserApiOutput(_, _, _))
  implicit val usersOutputJsonFormat = jsonFormat1(UsersApiOutput(_))
  implicit val replaceUserApiInputJsonFormat = jsonFormat4(ReplaceUserApiInput)

  implicit val actionPerformedJsonFormat = jsonFormat1(UserId)
}
