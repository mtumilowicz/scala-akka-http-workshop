package app.infrastructure.http.venue

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.gateway.venue.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.venue.out.VenueApiOutput
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object VenueJsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val venueOutJsonFormat: RootJsonFormat[VenueApiOutput] = 
      jsonFormat4(VenueApiOutput)
    implicit val venueInJsonFormat: RootJsonFormat[NewVenueApiInput] = 
      jsonFormat2(NewVenueApiInput)
    implicit val buyerIdInJsonFormat: RootJsonFormat[BuyerIdApiInput] = 
      jsonFormat1(BuyerIdApiInput)
  }