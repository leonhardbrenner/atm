package services

import org.junit.Test
import DatabaseFactory
import generated.model.SeedsDto
import kotlin.test.assertEquals

class SeedsServiceTests {
    //TODO - This is the result of bad factoring. SeedsDb needs to be injected.
    init {
        DatabaseFactory.init()
    }
    val seedsService = SeedsService()

    @Test
    fun testMySeeds() {
        val seeds = seedsService.getDetailedSeeds()
        assertEquals(
            SeedsDto.DetailedSeed(
                id = 0,
                companyId = "jss",
                seedId = "2588G",
                name = "Clemson Spineless",
                maturity = "60 Days",
                secondaryName = "Organic Okra Seed",
                description = null,
                image = "https://www.johnnyseeds.com/dw/image/v2/BBBW_PRD/on/demandware.static/-/Sites-jss-master/default/dw2915a7d4/images/products/vegetables/02588g_01_clemsonspineless.jpg?sw=387&cx=302&cy=0&cw=1196&ch=1196",
                link = "https://www.johnnyseeds.com/vegetables/okra/clemson-spineless-organic-okra-seed-2588G.html?cgid=okra"
            ),
            seeds[0]
        )
    }

    @Test
    fun testDetailedSeeds() {
        val detailedSeeds = seedsService.getDetailedSeeds()
        detailedSeeds
    }

}