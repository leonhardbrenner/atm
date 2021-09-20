package model

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
    val history: List<AtmDto.Transaction>? = null,
    val authorizationError: String? = null,
    val accountError: String? = null,
    val machineError: String? = null
)
