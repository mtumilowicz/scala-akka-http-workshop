package app.infrastructure

import app.domain.UserRegistry.ActionPerformed
import app.domain.{User, Users}
import app.gateway.out.{UserOutput, UsersOutput}
import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)

  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val userOutputJsonFormat = jsonFormat2(UserOutput(_, _))
  implicit val usersOutputJsonFormat = jsonFormat1(UsersOutput(_))
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
