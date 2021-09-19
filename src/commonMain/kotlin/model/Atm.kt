package model

import generated.model.AtmDto
import kotlinx.serialization.Serializable

typealias AccountId = String
typealias Amount = Double
typealias Pin = String
typealias Token = String
typealias SerialNumber = String

@Serializable
data class Reciept(
    val amount: Amount? = null,
    val balance: Amount? = null,
    val token: Token? = null,
    val history: List<AtmDto.Transaction>? = null,
    val accountError: String? = null,
    val machineError: String? = null
) {
    //XXX - Amount dispensed <=> deposited
    override fun toString() =
        """
        ${amount?.let { "Amount dispensed: $it; " }?: ""} 
        ${balance?.let { "Current balance: $it; " }?: ""} 
        ${history?.let { "History:" + it.joinToString(", ") + "; "}?: ""}
        ${accountError?.let { "Account Error: $it; " }?: ""} 
        ${machineError?.let { "Machine Error: $it; " }?: ""} 
""".trimIndent()
}
