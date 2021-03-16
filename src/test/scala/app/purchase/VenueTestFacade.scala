package app.purchase

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import app.domain.venue.VenueId
import app.gateway.venue.in.NewVenueApiInput
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import app.infrastructure.http.JsonFormats._

import java.util.UUID

object VenueTestFacade extends AnyWordSpec with ScalaFutures with ScalatestRouteTest {

  def createRandomVenue()(implicit route: Route): VenueId = {
    val venueInput = NewVenueApiInput(
      price = 500,
      name = "XYZ"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/${UUID.randomUUID()}").withEntity(venueEntity)

    request ~> route ~> check {
      VenueId(UUID.fromString(entityAs[String]))
    }
  }

  def createVenue(price: Int)(implicit route: Route): VenueId = {
    val venueInput = NewVenueApiInput(
      price = price,
      name = "ABC"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/${UUID.randomUUID()}").withEntity(venueEntity)

    request ~> route ~> check {
      VenueId(UUID.fromString(entityAs[String]))
    }
  }

}
