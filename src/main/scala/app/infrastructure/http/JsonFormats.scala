package app.infrastructure.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.domain.user.UserId
import app.gateway.user.in.{NewUserApiInput, ReplaceUserApiInput}
import app.gateway.user.out.{UserApiOutput, UsersApiOutput}
import app.gateway.venue.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.venue.out.VenueApiOutput
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
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

  implicit val venueOutJsonFormat: RootJsonFormat[VenueApiOutput] =
    jsonFormat4(VenueApiOutput)
  implicit val venueInJsonFormat: RootJsonFormat[NewVenueApiInput] =
    jsonFormat2(NewVenueApiInput)
  implicit val buyerIdInJsonFormat: RootJsonFormat[BuyerIdApiInput] =
    jsonFormat1(BuyerIdApiInput)
}
