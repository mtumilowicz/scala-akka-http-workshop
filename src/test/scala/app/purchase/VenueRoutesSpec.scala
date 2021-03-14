package app.purchase

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import app.domain.cash.NonNegativeAmount
import app.domain.user.NewUserInput
import app.gateway.venue.VenueRoutes
import app.gateway.venue.in.{BuyerIdApiInput, NewVenueApiInput}
import app.gateway.venue.out.VenueApiOutput
import app.infrastructure.config.{PurchaseConfig, UserConfig, VenueConfig}
import app.infrastructure.http.JsonFormats._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class VenueRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, TimeUnit.SECONDS))

  val venueService = VenueConfig.inMemoryService()
  val userService = UserConfig.inMemoryService()
  val purchaseConfig = PurchaseConfig.service(userService, venueService)
  val venueActor = testKit.spawn(VenueConfig.actor(venueService).behavior())
  val purchaseActor = testKit.spawn(PurchaseConfig.actor(purchaseConfig).behavior())
  lazy val routes = new VenueRoutes(
    venueActor,
    purchaseActor
  ).routes

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  "VenueRoutes" should {
    "return no venues if no present" in {
      //      when
      val request = HttpRequest(uri = "/venues")

      //      then
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[List[VenueApiOutput]] should be(List())
      }
    }

    "return venues if they are present" in {
      //      given
      val apiOutput = createRandomVenue()

      //      when
      val request = HttpRequest(uri = "/venues")

      //      then
      request ~> routes ~> check {
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
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val output = entityAs[String]
        output == id
      }
    }

    "get existing venue by id" in {
      //      given
      val id = createRandomVenue()

      //        when
      val get = Get(uri = "/venues/" + id)

      //        then
      get ~> routes ~> check {
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
      get ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "remove existing venue by id" in {
      //      given
      val id = createRandomVenue()

      //        when
      val delete = Delete(uri = "/venues/" + id)

      //        then
      delete ~> routes ~> check {
        status should ===(StatusCodes.OK)
        entityAs[String] should be(id)
      }
    }

    "remove not existing venue by id" in {
      //        when
      val delete = Delete(uri = s"/venues/${UUID.randomUUID().toString}")

      //        then
      delete ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "replace existing venue" in {
      //      given
      val id = createRandomVenue()
      val venuePut = NewVenueApiInput("DEF", 333)
      val venueEntity = Marshal(venuePut).to[MessageEntity].futureValue

      //        when
      val requestPut = Put(uri = "/venues/" + id).withEntity(venueEntity)

      //        then
      requestPut ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPut = entityAs[String]
        outputPut should be(id)
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
      requestPut ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPut = entityAs[String]
        outputPut should be(id)
      }
    }

    "buying fails when you cannot afford property" in {
      //      given
      val rynekGlowny = createRynekGlowny()
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(500))).id.raw
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue

      //        when
      val requestPost = Post(uri = s"/venues/${rynekGlowny}/buy").withEntity(buyerIdEntity)

      //        then
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val outputPost = entityAs[String]
        outputPost should be(s"${user1Id} can't afford Rynek Główny")
      }
    }

    "venue without owner: buying succeeds when you can afford property" in {
      //      given
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(2000))).id.raw
      val rynekGlowny = createRynekGlowny()
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue

      //        when
      val requestPost = Post(uri = s"/venues/${rynekGlowny}/buy").withEntity(buyerIdEntity)

      //        then
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPost = entityAs[String]
        outputPost should be(s"Rynek Główny was bought by ${user1Id} for 1000")
      }
    }

    "venue with owner: buying succeeds when you can afford property" in {
      //      given
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(500))).id.raw
      val user2Id = userService.save(NewUserInput("b", NonNegativeAmount(1000))).id.raw

      //      and
      val venue = createRandomVenue()
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

      //      when
      val nextBuyerIdInput = BuyerIdApiInput(user2Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)

      //        then
      requestPost2 ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPost = entityAs[String]
        outputPost should be(s"XYZ was bought by $user2Id for 500")
      }

    }

    "venue with owner: buying fails if buyer same as owner" in {
      //      given
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(500))).id.raw
      val user2Id = userService.save(NewUserInput("b", NonNegativeAmount(1000))).id.raw

      //      and
      val venue = createRandomVenue()
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

      //      when
      val nextBuyerIdInput = BuyerIdApiInput(user1Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)

      //        then
      requestPost2 ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val outputPost = entityAs[String]
        outputPost should be(s"User $user1Id cannot buy from himself.")
      }
    }

    "selling property increases budget of seller" in {
      //      given
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(500))).id.raw
      val user2Id = userService.save(NewUserInput("b", NonNegativeAmount(1000))).id.raw

      //      and
      val venue = createRandomVenue()
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

      //      and
      val nextBuyerIdInput = BuyerIdApiInput(user2Id)
      val nextBuyerIdEntity = Marshal(nextBuyerIdInput).to[MessageEntity].futureValue
      val requestPost2 = Post(uri = s"/venues/${venue}/buy").withEntity(nextBuyerIdEntity)
      requestPost2 ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

      //      when
      val nextVenue = createRandomVenue()
      val requestPost3 = Post(uri = s"/venues/${nextVenue}/buy").withEntity(buyerIdEntity)
      requestPost3 ~> routes ~> check {
        status should ===(StatusCodes.OK)

        val outputPost = entityAs[String]
        outputPost should be(s"XYZ was bought by $user1Id for 500")
      }
    }

    "buying property decreases budget of buyer" in {
      //      given
      val user1Id = userService.save(NewUserInput("a", NonNegativeAmount(500))).id.raw

      //      and
      val venue = createRandomVenue()
      val buyerIdInput = BuyerIdApiInput(user1Id)
      val buyerIdEntity = Marshal(buyerIdInput).to[MessageEntity].futureValue
      val requestPost = Post(uri = s"/venues/${venue}/buy").withEntity(buyerIdEntity)
      requestPost ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }

      //      and
      val nextVenue = createRandomVenue()
      val requestPost2 = Post(uri = s"/venues/${nextVenue}/buy").withEntity(buyerIdEntity)
      requestPost2 ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)

        val outputPost = entityAs[String]
        outputPost should be(s"${user1Id} can't afford XYZ")
      }
    }
  }

  def createRandomVenue(): String = {
    val venueInput = NewVenueApiInput(
      price = 500,
      name = "XYZ"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/${UUID.randomUUID()}").withEntity(venueEntity)

    request ~> routes ~> check {
      entityAs[String]
    }
  }

  def createRynekGlowny(): String = {
    val venueInput = NewVenueApiInput(
      price = 1000,
      name = "Rynek Główny"
    )

    val venueEntity = Marshal(venueInput).to[MessageEntity].futureValue

    val request = Put(s"/venues/acf869a2-9f1e-4f9c-b95d-a0f1932e3428").withEntity(venueEntity)

    request ~> routes ~> check {
      entityAs[String]
    }
  }
}