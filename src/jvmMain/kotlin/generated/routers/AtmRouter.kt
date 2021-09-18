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

        //login
        get {
            val accountId = call.parameters["accountId"]!!
            val pin = call.parameters["pin"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.login(accountId, pin) )
        }

        //withdraw
        post {
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

        //deposit
        post {
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

        //balance
        get {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.balance(accountId, token) )
        }

        //history
        get {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.history(accountId, token) )
        }

        //logout
        get {
            val accountId = call.parameters["accountId"]!!
            val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond( atmService.logout(accountId, token) )
        }

    }
}
