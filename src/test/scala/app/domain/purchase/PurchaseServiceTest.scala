package app.domain.purchase

import app.domain.cash.NonNegativeAmount
import app.domain.purchase.error.{CantAffordBuyingVenueError, CantBuyFromYourselfError}
import app.domain.user.{UserBalance, UserId}
import app.domain.venue.{Venue, VenueIdService}
import app.infrastructure.config.{PurchaseConfig, UserBalanceConfig, VenueConfig}
import org.scalatest.EitherValues._
import org.scalatest.GivenWhenThen
import org.scalatest.OptionValues._
import org.scalatest.featurespec.AsyncFeatureSpec
import org.scalatest.matchers.should.Matchers.{a, convertToAnyShouldWrapper}

import java.util.UUID

class PurchaseServiceTest extends AsyncFeatureSpec with GivenWhenThen {

  val venueIdService = new VenueIdService()

  Feature("purchasing venue") {
    Scenario("when venue has no owner") {
      Given("prepare in memory services")
      val userBalanceService = UserBalanceConfig.inMemoryService()
      val venueService = VenueConfig.inMemoryService()
      val purchaseService = PurchaseConfig.service(userBalanceService, venueService)

      And("create venue")
      val venueId = venueIdService.generate()
      val venue = Venue(
        id = venueId,
        name = "venue1",
        price = NonNegativeAmount(100).value,
        owner = Option.empty
      )
      venueService.save(venue)

      And("create user balance")
      val userId = UserId(UUID.randomUUID().toString)
      val userBalance = UserBalance(
        id = userId,
        budget = NonNegativeAmount(150).value
      )
      userBalanceService.save(userBalance)

      When("buying property")
      val newPurchase = NewPurchase(buyer = userId, venue = venueId)
      val result = purchaseService.purchase(newPurchase).value

      Then("verify successful buy")
      result map {
        _.value
      } map { boughtVenue =>
        boughtVenue.id shouldBe venueId
        boughtVenue.name shouldBe "venue1"
        boughtVenue.price shouldBe NonNegativeAmount(100).value
        boughtVenue.owner.value shouldBe userId
      }

      And("verify that venue has owner")
      val venueFromDb = venueService.findById(venueId).value
      venueFromDb map {
        _.value
      } map {
        _.owner.value shouldBe userId
      }

      And("verify that user balance is debited")
      val userBalanceFroDb = userBalanceService.findById(userId).value
      userBalanceFroDb map {
        _.value
      } map {
        _.budget shouldBe NonNegativeAmount(50).value
      }
    }

    Scenario("buying from other owner") {
      Given("prepare in memory services")
      val userBalanceService = UserBalanceConfig.inMemoryService()
      val venueService = VenueConfig.inMemoryService()
      val purchaseService = PurchaseConfig.service(userBalanceService, venueService)

      And("create first owner user balance")
      val firstOwnerId = UserId(UUID.randomUUID().toString)
      val firstOwnerBalance = UserBalance(
        id = firstOwnerId,
        budget = NonNegativeAmount(150).value
      )
      userBalanceService.save(firstOwnerBalance)

      And("create venue")
      val venueId = venueIdService.generate()
      val venue = Venue(
        id = venueId,
        name = "venue1",
        price = NonNegativeAmount(100).value,
        owner = Some(firstOwnerId)
      )
      venueService.save(venue)

      And("create next owner user balance")
      val nextOwnerId = UserId(UUID.randomUUID().toString)
      val nextOwnerBalance = UserBalance(
        id = nextOwnerId,
        budget = NonNegativeAmount(250).value
      )
      userBalanceService.save(nextOwnerBalance)

      When("buying property")
      val newPurchase = NewPurchase(buyer = nextOwnerId, venue = venueId)
      val result = purchaseService.purchase(newPurchase)

      Then("verify successful buy")
      val boughtVenue = result.value
      boughtVenue.map(_.value).map(
        _.owner.value shouldBe nextOwnerId
      )

      And("verify that venue has owner")
      val venueFromDb = venueService.findById(venueId).value
      venueFromDb.map(_.value) map {
        _.owner.value shouldBe nextOwnerId
      }

      And("verify that first owner balance is credited")
      val firstOwnerBalanceFroDb = userBalanceService.findById(firstOwnerId).value
      firstOwnerBalanceFroDb.map(_.value).map(
        _.budget shouldBe NonNegativeAmount(250).value
      )

      And("verify that next owner balance is debited")
      val nextOwnerBalanceFroDb = userBalanceService.findById(nextOwnerId).value
      nextOwnerBalanceFroDb.map(_.value).map(
        _.budget shouldBe NonNegativeAmount(150).value
      )
    }

    Scenario("when user cannot afford venue") {
      Given("prepare in memory services")
      val userBalanceService = UserBalanceConfig.inMemoryService()
      val venueService = VenueConfig.inMemoryService()
      val purchaseService = PurchaseConfig.service(userBalanceService, venueService)

      And("create venue")
      val venueId = venueIdService.generate()
      val venue = Venue(
        id = venueId,
        name = "venue1",
        price = NonNegativeAmount(100).value,
        owner = Option.empty
      )
      venueService.save(venue)

      And("create user with low balance")
      val userId = UserId(UUID.randomUUID().toString)
      val userBalance = UserBalance(
        id = userId,
        budget = NonNegativeAmount(50).value
      )
      userBalanceService.save(userBalance)

      When("buying property")
      val newPurchase = NewPurchase(buyer = userId, venue = venueId)
      val result = purchaseService.purchase(newPurchase)

      Then("verify successful buy")
      result.value.map(_.left.value).map(
        _ shouldBe a[CantAffordBuyingVenueError]
      )
    }

    Scenario("purchase fails if buyer same as owner") {
      Given("prepare in memory services")
      val userBalanceService = UserBalanceConfig.inMemoryService()
      val venueService = VenueConfig.inMemoryService()
      val purchaseService = PurchaseConfig.service(userBalanceService, venueService)

      And("create first owner user balance")
      val userId = UserId(UUID.randomUUID().toString)
      val userBalance = UserBalance(
        id = userId,
        budget = NonNegativeAmount(150).value
      )
      userBalanceService.save(userBalance)

      And("create venue")
      val venueId = venueIdService.generate()
      val venue = Venue(
        id = venueId,
        name = "venue1",
        price = NonNegativeAmount(100).value,
        owner = Some(userId)
      )
      venueService.save(venue)

      When("buying property")
      val newPurchase = NewPurchase(buyer = userId, venue = venueId)
      val result = purchaseService.purchase(newPurchase)

      Then("verify that you cannot buy from yourself")
      result.value.map(_.left.value).map(
        _ shouldBe a[CantBuyFromYourselfError]
      )
    }
  }

}
