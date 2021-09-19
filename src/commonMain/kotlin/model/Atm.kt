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
    val accountError: String = "",
    val machineError: String = ""
) {
    override fun toString() =
        if (history != null)
            history.joinToString("\n")
        else if (amount == null) {
            """
            $accountError
            $machineError    
            """.trimIndent()

        } else {
            """
            Amount dispensed: $amount
            Current balance: <balance>
            
            $accountError
            $machineError
            """.trimIndent().trimEnd()
        }
}
