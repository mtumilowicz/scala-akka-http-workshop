package app.infrastructure.http.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.domain.user.UserId
import app.gateway.user.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.user.out.{UserApiOutput, UsersApiOutput}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object UserJsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val newUserApiInputJsonFormat: RootJsonFormat[NewUserApiInput] =
    jsonFormat2(NewUserApiInput)
  implicit val userOutputJsonFormat: RootJsonFormat[UserApiOutput] =
    jsonFormat3(UserApiOutput)
  implicit val usersOutputJsonFormat: RootJsonFormat[UsersApiOutput] =
    jsonFormat1(UsersApiOutput)
  implicit val replaceUserApiInputJsonFormat: RootJsonFormat[ReplaceUserApiInput] =
    jsonFormat2(ReplaceUserApiInput)
  implicit val actionPerformedJsonFormat: RootJsonFormat[UserId] =
    jsonFormat1(UserId)
}