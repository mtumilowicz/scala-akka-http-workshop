package app.infrastructure

import app.domain.user.UserId
import app.gateway.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import spray.json.DefaultJsonProtocol

object JsonFormats {

  import DefaultJsonProtocol._

  implicit val newUserApiInputJsonFormat = jsonFormat2(NewUserApiInput)
  implicit val userOutputJsonFormat = jsonFormat3(UserApiOutput)
  implicit val usersOutputJsonFormat = jsonFormat1(UsersApiOutput)
  implicit val replaceUserApiInputJsonFormat = jsonFormat2(ReplaceUserApiInput)

  implicit val actionPerformedJsonFormat = jsonFormat1(UserId)
}
