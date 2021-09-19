import generated.model.AtmDto
import io.ktor.client.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*

import kotlinx.browser.window
import model.*


val endpoint = window.location.origin // only needed until https://github.com/ktorio/ktor/issues/1695 is resolved

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

object Api {
    //login 1434597300 4557
    suspend fun login(accountId: AccountId, pin: Pin) =
        jsonClient.get<Reciept>(endpoint + "/accounts/$accountId/login") {
            parameter("pin", pin)
        }

    suspend fun withdraw(accountId: AccountId, token: Token, amount: Amount) =
        jsonClient.post<Reciept>(endpoint + "/accounts/$accountId/withdraw") {
            parameter("token", token)
            parameter("amount", amount)
        }

    suspend fun deposit(accountId: AccountId, token: Token, amount: Amount) =
        jsonClient.post<Reciept>(endpoint + "/accounts/$accountId/deposit") {
            parameter("token", token)
            parameter("amount", amount)
        }

    suspend fun balance(accountId: AccountId, token: Token) =
        jsonClient.get<Reciept>(endpoint + "/account/$accountId/balance") {
            parameter("token", token)
        }

    suspend fun history(accountId: AccountId, token: Token) =
        jsonClient.get<Reciept>(endpoint + "/accounts/$accountId/history") {
            parameter("token", token)
        }

    suspend fun logout(accountId: AccountId, token: Token) =
        jsonClient.put<Reciept>(endpoint + "/accounts/$accountId/logout") {
            parameter("token", token)
        }

}