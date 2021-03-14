package app.infrastructure.config

import app.domain.purchase.PurchaseService
import app.domain.user.UserService
import app.domain.venue.VenueService
import app.infrastructure.actor.PurchaseActor

object PurchaseConfig {

  def inMemoryService(): PurchaseService =
    service(
      userService = UserConfig.inMemoryService,
      venueService = VenueConfig.inMemoryService
    )

  def service(userService: UserService, venueService: VenueService): PurchaseService =
    new PurchaseService(
      userService = userService,
      venueService = venueService
    )

  def actor(purchaseService: PurchaseService): PurchaseActor =
    new PurchaseActor(purchaseService)

}
