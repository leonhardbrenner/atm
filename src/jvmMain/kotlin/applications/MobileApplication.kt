package applications

import com.google.inject.AbstractModule
import generated.model.SeedsDto
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.SeedsService
import javax.inject.Inject

class MobileApplication @Inject constructor(val seedsService: SeedsService) {

    object Module : AbstractModule()

    fun routesFrom(routing: Routing) = routing.apply {
        //route(SeedsDto.MySeeds.path) {
        //    get {
        //        //XXX - You will need an outbound route which creates a Dto for us. This would be a good use of extensions.
        //        call.respond(seedsService.mySeeds)
        //    }
        //}
        //route(SeedsDto.DetailedSeed.path) {
        //    get {
        //        call.respond(seedsService.getDetailedSeeds())
        //    }
        //}
        //route(SeedsDto.SeedCategory.path) {
        //    get {
        //        call.respond(seedsService.getCategories())
        //    }
        //}
    }

}