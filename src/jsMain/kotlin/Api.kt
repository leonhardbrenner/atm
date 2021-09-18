import generated.model.AtmDto
import io.ktor.client.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.http.*

import kotlinx.browser.window

val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

class Api {

    //Todo - Post transaction with reciept ** I think this is one endpoint
    //suspend fun create(accountId: Int, name: String) {
    //    jsonClient.post<Unit>(endpoint + AtmDto.Transaction.path) {
    //        contentType(ContentType.Application.Json)
    //        body = AtmDto.Transaction(-1, "Y")
    //
    //        parameter("parentId", parentId)
    //        parameter("name", name)
    //    }
    //}

    /*
    suspend fun index(): List<AtmDto.Transaction> {
        return jsonClient.get(endpoint + AtmDto.Transaction.path)
    }

    suspend fun new() { TODO("Form data defaults. Use a template class.") }

    suspend fun create(parentId: Int, name: String) {
        jsonClient.post<Unit>(endpoint + TransactionDto.Chore.path) {
            parameter("parentId", parentId)
            parameter("name", name)
        }
    }

    suspend fun edit() { TODO("Form data defaults. Use a template class.") }

    //Todo - implement edit which has loads existing values

    suspend fun move(id: Int, parentId: Int) {
        jsonClient.put<Unit>(endpoint + TransactionDto.Chore.path + "/$id/move") {
            //Example of how to send a complexType
            //data class Node(val id: Int, val name: String)
            //contentType(ContentType.Application.Json)
            //body = Node(1, "Y")
            parameter("parentId", parentId)
        }
    }

    suspend fun destroy(choreId: Int) {
        jsonClient.delete<Unit>(endpoint + SeedsDto.Chore.path + "/${choreId}")
    }
    */
}