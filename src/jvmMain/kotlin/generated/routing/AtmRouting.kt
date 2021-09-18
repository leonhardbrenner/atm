package generated.routing

import generated.model.AtmDto
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.routing.route
import javax.inject.Inject
import services.AtmSession

class AtmRouting@Inject constructor(
    val atmSession: AtmSession
) {
    fun routes(routing: Routing) = routing.route(AtmDto.Transaction.path) {

        post {
            val response = try {
                val request = call.parameters["request"]!!
                atmSession.handleMessage(request)
            } catch (ex: Exception) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(response)
        }

    }
}
