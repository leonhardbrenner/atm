package generated.routing

import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.AtmDto.AuthorizationPin
import generated.model.AtmDto.AuthorizationToken
import generated.model.AtmDto.Ledger
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
import services.atm.AuthorizationPinService
import services.atm.AuthorizationTokenService
import services.atm.LedgerService

class AtmRouting {
  class AuthorizationPin @Inject constructor(
    val dao: AtmDao.AuthorizationPin,
    val service: AuthorizationPinService
  ) {
    fun unmarshal(parameters: Parameters) = AtmDto.AuthorizationPin(
    parameters["id"]?.toInt()?: -1, 
    parameters["accountId"] ?: throw Exception("BadRequest"), 
    parameters["pin"] ?: throw Exception("BadRequest"))

    fun routes(routing: Routing) = routing.route(AtmDto.AuthorizationPin.path) {
     
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

  class AuthorizationToken @Inject constructor(
    val dao: AtmDao.AuthorizationToken,
    val service: AuthorizationTokenService
  ) {
    fun unmarshal(parameters: Parameters) = AtmDto.AuthorizationToken(
    parameters["id"]?.toInt()?: -1, 
    parameters["accountId"] ?: throw Exception("BadRequest"), 
    parameters["token"] ?: throw Exception("BadRequest"), 
    parameters["expiration"] ?: throw Exception("BadRequest"))

    fun routes(routing: Routing) = routing.route(AtmDto.AuthorizationToken.path) {
     
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

  class Ledger @Inject constructor(
    val dao: AtmDao.Ledger,
    val service: LedgerService
  ) {
    fun unmarshal(parameters: Parameters) = AtmDto.Ledger(
    parameters["id"]?.toInt()?: -1, 
    parameters["accountId"] ?: throw Exception("BadRequest"), 
    parameters["balance"] ?: throw Exception("BadRequest"))

    fun routes(routing: Routing) = routing.route(AtmDto.Ledger.path) {
     
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
