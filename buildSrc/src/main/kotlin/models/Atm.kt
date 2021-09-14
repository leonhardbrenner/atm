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
        val expiration: String
    )

    class Ledger(
        val id: Int,
        val accountId: String,
        val balance: Double
    )

}
