package generated.routers

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import services.AtmService
import javax.inject.Inject
import kotlin.text.get

class AtmRouter @Inject constructor(
    val atmService: AtmService
) {
    fun routes(routing: Routing) = routing.route("/accounts/{accountId}") {

        get("/login") {
            val accountId = call.parameters["accountId"]!!
            val pin = call.parameters["pin"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.login(accountId, pin) )
        }

        post("/withdraw") {
            call.respond(
                try {
                    val accountId = call.parameters["accountId"]!!
                    val token = call.parameters["token"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val amount = call.parameters["amount"]?.toDouble() ?: return@post call.respond(HttpStatusCode.BadRequest)
                    atmService.withdraw(accountId, token, amount)
                } catch (ex: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
            )
        }

        post("/deposit") {
            call.respond(
                try {
                    val accountId = call.parameters["accountId"]!!
                    val token = call.parameters["token"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val amount = call.parameters["amount"]?.toDouble() ?: return@post call.respond(HttpStatusCode.BadRequest)
                    atmService.deposit(accountId, token, amount)
                } catch (ex: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest)
                }
            )
        }

        get("/balance") {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.balance(accountId, token) )
        }

        get("/history") {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.history(accountId, token) )
        }

        put("/logout") {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            atmService.logout(accountId, token)
            call.respond( HttpStatusCode.OK )
        }

    }
}
