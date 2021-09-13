package generated.routing

import generated.dao.SeedsDao
import generated.model.SeedsDto
import generated.model.SeedsDto.DetailedSeed
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import javax.inject.Inject
import org.jetbrains.exposed.sql.transactions.transaction
import services.seeds.DetailedSeedService

class SeedsRouting {
  class DetailedSeed @Inject constructor(
    val dao: SeedsDao.DetailedSeed,
    val service: DetailedSeedService
  ) {
    fun unmarshal(parameters: Parameters) = SeedsDto.DetailedSeed(
    parameters["id"]?.toInt()?: -1, 
    parameters["companyId"] ?: throw Exception("BadRequest"), 
    parameters["seedId"] ?: throw Exception("BadRequest"), 
    parameters["name"] ?: throw Exception("BadRequest"), 
    parameters["maturity"], 
    parameters["secondaryName"], 
    parameters["description"], 
    parameters["image"], 
    parameters["link"])

    fun routes(routing: Routing) = routing.route(SeedsDto.DetailedSeed.path) {
     
        get {
            call.respond(transaction { service.index() })
        }
        
        post {
            call.respond(
                try {
                    transaction { service.create(unmarshal(call.parameters)) }
                } catch (ex: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
            )
        }


        
        put("/{id}") {
            call.respond(
                try {
                    transaction { service.update(unmarshal(call.parameters)) }
                } catch (ex: Exception) {
                    return@put call.respond(HttpStatusCode.BadRequest)
                }
            )
        }
        
        delete("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
            transaction {
                service.destroy(id)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
  }
}
