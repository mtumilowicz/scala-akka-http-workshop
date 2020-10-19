package app.infrastructure

import app.domain.UserService.ActionPerformed
import app.gateway.in.NewUserApiInput
import app.gateway.out.{UserApiOutput, UsersApiOutput}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val newUserApiInputJsonFormat = jsonFormat3(NewUserApiInput)
  implicit val userOutputJsonFormat = jsonFormat2(UserApiOutput(_, _))
  implicit val usersOutputJsonFormat = jsonFormat1(UsersApiOutput(_))

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
