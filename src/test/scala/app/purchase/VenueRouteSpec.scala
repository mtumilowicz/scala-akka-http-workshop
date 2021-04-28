package app.purchase

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import app.domain.user.UserId
import app.gateway.venue.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.venue.out.VenueApiOutput
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}
import app.infrastructure.http.JsonFormats._
import app.infrastructure.http.user.UserRouteConfig
import app.infrastructure.http.venue.VenueRouteConfig
import app.user.UserTestFacade
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class VenueRouteSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, TimeUnit.SECONDS))

  implicit val route = prepareRoute()

  def prepareRoute(): Route = {
    val venueService = VenueConfig.inMemoryService()
    val userService = UserConfig.inMemoryService()
    val purchaseService = PurchaseConfig.service(userService, venueService)
    val venueRoute = VenueRouteConfig.config(venueService, purchaseService).route
    val userRoute = UserRouteConfig.config(userService).route
    venueRoute ~ userRoute
  }

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  "VenueRoutes" should {
    "return no venues if no present" in {
      //      when
      val request = HttpRequest(uri = "/venues")

      //      then
      request ~> route ~> check {
        status should ===(StatusCodes.OK)
        entityAs[List[VenueApiOutput]] should be(List())
      }
    }

    "return venues if they are present" in {
      //      given
      VenueTestFacade.createRandomVenue()

      //      when
      val request = HttpRequest(uri = "/venues")

      //      then
      request ~> route ~> check {
        status should ===(StatusCodes.OK)
        entityAs[List[VenueApiOutput]] should not be empty
      }
    }

    "create venue" in {
      //      given
      val venueInput = NewVenueApiInput(
        price = 100,
        name = "ABC"
      )
      val id = UUID.randomUUID().toString

      val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

      val request = Put(s"/venues/$id").withEntity(venueEntity)

      //      then
      request ~> route ~> check {
        status should ===(StatusCodes.OK)

        val output = entityAs[VenueApiOutput]
        output.id should be(id)
      }
    }

    "get existing venue by id" in {
      //      given
      val id = VenueTestFacade.createRandomVenue().id

      //        when
      val get = Get(uri = "/venues/" + id)

      //        then
      get ~> route ~> check {
        status should ===(StatusCodes.OK)
        val outputOfGet = entityAs[VenueApiOutput]
        outputOfGet.id should be(id)
        outputOfGet.name should be("XYZ")
        outputOfGet.price should be(500)
        outputOfGet.owner should be(None)
      }
    }

    "get not existing venue by id" in {
      //        when
      val get = Get(uri = s"/venues/${UUID.randomUUID().toString}")

      //        then
      get ~> route ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "remove existing venue by id" in {
      //      given
      val id = VenueTestFacade.createRandomVenue().id

      //        when
      val delete = Delete(uri = "/venues/" + id)

      //        then
      delete ~> route ~> check {
        status should ===(StatusCodes.OK)
        entityAs[String] should be(id)
      }

      // and
      val get = Get(uri = s"/venues/${id}")
      get ~> route ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "remove not existing venue by id" in {
      //        when
      val delete = Delete(uri = s"/venues/${UUID.randomUUID().toString}")

      //        then
      delete ~> route ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "replace existing venue" in {
      //      given
      val id = VenueTestFacade.createRandomVenue().id
      val venuePut = NewVenueApiInput("DEF", 333)
      val venueEntity = Marshal(venuePut).to[MessageEntity].futureValue

      //        when
      val requestPut = Put(uri = "/venues/" + id).withEntity(venueEntity)

      //        then
      requestPut ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputPut = entityAs[VenueApiOutput]
        outputPut.id should be(id)
      }

      // and
      val get = Get(uri = s"/venues/${id}")
      get ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputOfGet = entityAs[VenueApiOutput]
        outputOfGet.id should be(id)
        outputOfGet.name should be("DEF")
        outputOfGet.price should be(333)
        outputOfGet.owner should be(None)
      }
    }

    "insert venue" in {
      //      given
      val venuePut = NewVenueApiInput("AAA", 123)
      val venueEntity = Marshal(venuePut).to[MessageEntity].futureValue
      val id = UUID.randomUUID().toString

      //        when
      val requestPut = Put(uri = s"/venues/$id").withEntity(venueEntity)

      //        then
      requestPut ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputPut = entityAs[VenueApiOutput]
        outputPut.id should be(id)
      }

      // and
      val get = Get(uri = s"/venues/${id}")
      get ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputOfGet = entityAs[VenueApiOutput]
        outputOfGet.id should be(id)
        outputOfGet.name should be("AAA")
        outputOfGet.price should be(123)
        outputOfGet.owner should be(None)
      }
    }

    "buying fails when you cannot afford property" in {
      //      given
      val venue = VenueTestFacade.createVenue(1000).id
      val user1Id = UserTestFacade.createUser(500).id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue

      //        when
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)

      //        then
      requestPost ~> route ~> check {
        status should ===(StatusCodes.BadRequest)

        val outputPost = entityAs[String]
        outputPost should be(s"${user1Id} can't afford ABC")
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 500
    }

    "venue without owner: buying succeeds when you can afford property" in {
      //      given
      val user1Id = UserTestFacade.createUser(2000).id
      val venueId = VenueTestFacade.createVenue(1000).id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue

      //        when
      val requestPost = Post(uri = s"/venues/$venueId/buy").withEntity(buyerIdEntity)

      //        then
      requestPost ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputPost = entityAs[String]
        outputPost should be(s"ABC was bought by ${user1Id} for 1000")
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 1000
    }

    "venue with owner: buying succeeds when you can afford property" in {
      //      given
      val user1Id = UserTestFacade.createUser(500).id
      val user2Id = UserTestFacade.createUser(1000).id

      //      and
      val venue = VenueTestFacade.createVenue(500).id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> route ~> check {
        status should ===(StatusCodes.OK)
      }

      //      when
      val nextBuyerIdInput = BuyerIdApiInput(user2Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)

      //        then
      requestPost2 ~> route ~> check {
        status should ===(StatusCodes.OK)

        val outputPost = entityAs[String]
        outputPost should be(s"ABC was bought by $user2Id for 500")
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 500
      UserTestFacade.getUser(UserId(user2Id)).budget shouldBe 500
    }

    "venue with owner: buying fails if buyer same as owner" in {
      //      given
      val user1Id = UserTestFacade.createUser(500).id

      //      and
      val venue = VenueTestFacade.createRandomVenue().id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> route ~> check {
        status should ===(StatusCodes.OK)
      }

      //      when
      val nextBuyerIdInput = BuyerIdApiInput(user1Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)

      //        then
      requestPost2 ~> route ~> check {
        status should ===(StatusCodes.BadRequest)

        val outputPost = entityAs[String]
        outputPost should be(s"User $user1Id cannot buy from himself.")
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 0
    }

    "selling property increases budget of seller" in {
      //      given
      val user1Id = UserTestFacade.createUser(500).id
      val user2Id = UserTestFacade.createUser(1000).id

      //      and
      val venue = VenueTestFacade.createRandomVenue().id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> route ~> check {
        status should ===(StatusCodes.OK)
      }

      //      and
      val nextBuyerIdInput = BuyerIdApiInput(user2Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)
      requestPost2 ~> route ~> check {
        status should ===(StatusCodes.OK)
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 500
    }

    "buying property decreases budget of buyer" in {
      //      given
      val user1Id = UserTestFacade.createUser(500).id

      //      when
      val venue = VenueTestFacade.createVenue(300).id
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> route ~> check {
        status should ===(StatusCodes.OK)
      }

      // and
      UserTestFacade.getUser(UserId(user1Id)).budget shouldBe 200
    }
  }
}