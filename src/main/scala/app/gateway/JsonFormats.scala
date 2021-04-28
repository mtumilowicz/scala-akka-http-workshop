package app.gateway

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.gateway.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.out.VenueApiOutput
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val venueOutJsonFormat: RootJsonFormat[VenueApiOutput] = jsonFormat4(VenueApiOutput)
  implicit val venueInJsonFormat: RootJsonFormat[NewVenueApiInput] = jsonFormat2(NewVenueApiInput)
  implicit val buyerIdInJsonFormat: RootJsonFormat[BuyerIdApiInput] = jsonFormat1(BuyerIdApiInput)
}
