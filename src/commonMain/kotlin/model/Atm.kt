package model

import generated.model.Atm
import generated.model.AtmDto
import kotlinx.serialization.Serializable

typealias AccountId = String
typealias Amount = Double
typealias Pin = String
typealias Token = String
typealias SerialNumber = String

@Serializable
data class Response(
    val amount: Amount? = null,
    val balance: Amount? = null,
    val token: Token? = null,
    val history: List<Transaction>? = null,
    val authorizationError: String? = null,
    val accountError: String? = null,
    val machineError: String? = null
)

@Serializable
data class Transaction(
    val source: AtmDto.Transaction,
    val formatedDatetime: String //Todo - format on client and store timestampe as OffsetDateTime.
): Atm.Transaction by source