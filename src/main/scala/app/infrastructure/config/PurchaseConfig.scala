package app.infrastructure.config

import app.domain.purchase.PurchaseService
import app.domain.user.UserBalanceService
import app.domain.venue.VenueService

object PurchaseConfig {

  def inMemoryService(): PurchaseService =
    service(
      userBalanceService = UserBalanceConfig.inMemoryService(),
      venueService = VenueConfig.inMemoryService()
    )

  def service(userBalanceService: UserBalanceService, venueService: VenueService): PurchaseService =
    new PurchaseService(
      userBalanceService = userBalanceService,
      venueService = venueService
    )

}
