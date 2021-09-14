package generated.model

import kotlin.Double
import kotlin.Int
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

    val expiration: String
  }

  interface Ledger {
    val id: Int

    val accountId: String

    val balance: Double
  }
}
