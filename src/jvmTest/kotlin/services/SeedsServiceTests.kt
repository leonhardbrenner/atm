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
    //val seedsService = SeedsService()

    @Test
    fun testDetailedSeeds() {
        //val detailedSeeds = seedsService.getDetailedSeeds()
        //detailedSeeds
    }

}