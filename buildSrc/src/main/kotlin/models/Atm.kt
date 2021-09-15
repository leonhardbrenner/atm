package models

interface Atm {

    class AuthorizationPin(
        val id: Int,
        val accountId: String,
        val pin: String
    )

    class AuthorizationToken( //Make this Node
        val id: Int,
        val accountId: String,
        val token: String,
        val expiration: Long
    )

    class Ledger(
        val id: Int,
        val accountId: String,
        val balance: Double
    )

    class Transaction(
        val id: Int,
        val accountId: String,
        val timestamp: Long,
        val amount: Double,
        val balance: Double
    )

    class Machine(
        val id: Int,
        val serialNumber: String,
        val balance: String
    )

}
