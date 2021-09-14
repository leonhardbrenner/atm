package generated.model

import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String

interface Atm {
  interface AuthorizationPin {
    val id: Int

    val accountId: String

    val pin: String
  }

  interface AuthorizationToken {
    val id: Int

    val accountId: String

    val token: String

    val expiration: Long
  }

  interface Ledger {
    val id: Int

    val accountId: String

    val balance: Double
  }

  interface Transaction {
    val timestamp: Long

    val amount: Double

    val balance: Double
  }
}
