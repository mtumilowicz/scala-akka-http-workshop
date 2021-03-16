package app.purchase

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import app.gateway.venue.in.NewVenueApiInput
import app.gateway.venue.out.VenueApiOutput
import app.infrastructure.http.JsonFormats._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

object VenueTestFacade extends AnyWordSpec with ScalaFutures with ScalatestRouteTest {

  def createRandomVenue()(implicit route: Route): VenueApiOutput = {
    val venueInput = NewVenueApiInput(
      price = 500,
      name = "XYZ"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/${UUID.randomUUID()}").withEntity(venueEntity)

    request ~> route ~> check {
      entityAs[VenueApiOutput]
    }
  }

  def createVenue(price: Int)(implicit route: Route): VenueApiOutput = {
    val venueInput = NewVenueApiInput(
      price = price,
      name = "ABC"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/${UUID.randomUUID()}").withEntity(venueEntity)

    request ~> route ~> check {
      entityAs[VenueApiOutput]
    }
  }

}
