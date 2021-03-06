package generated.model

import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable

interface AtmDto {
  @Serializable
  data class AuthorizationPin(
    override val id: Int,
    override val accountId: String,
    override val pin: String
  ) : Atm.AuthorizationPin {
    companion object {
      const val path: String = "/Atm/AuthorizationPin"

      fun create(source: Atm.AuthorizationPin) = AtmDto.AuthorizationPin(source.id,
          source.accountId, source.pin)}
  }

  @Serializable
  data class AuthorizationToken(
    override val id: Int,
    override val accountId: String,
    override val token: String,
    override val expiration: Long
  ) : Atm.AuthorizationToken {
    companion object {
      const val path: String = "/Atm/AuthorizationToken"

      fun create(source: Atm.AuthorizationToken) = AtmDto.AuthorizationToken(source.id,
          source.accountId, source.token, source.expiration)}
  }

  @Serializable
  data class Ledger(
    override val id: Int,
    override val accountId: String,
    override val balance: Double
  ) : Atm.Ledger {
    companion object {
      const val path: String = "/Atm/Ledger"

      fun create(source: Atm.Ledger) = AtmDto.Ledger(source.id, source.accountId, source.balance)}
  }

  @Serializable
  data class Machine(
    override val id: Int,
    override val serialNumber: String,
    override val balance: Double
  ) : Atm.Machine {
    companion object {
      const val path: String = "/Atm/Machine"

      fun create(source: Atm.Machine) = AtmDto.Machine(source.id, source.serialNumber,
          source.balance)}
  }

  @Serializable
  data class Transaction(
    override val id: Int,
    override val accountId: String,
    override val timestamp: Long,
    override val amount: Double,
    override val balance: Double
  ) : Atm.Transaction {
    companion object {
      const val path: String = "/Atm/Transaction"

      fun create(source: Atm.Transaction) = AtmDto.Transaction(source.id, source.accountId,
          source.timestamp, source.amount, source.balance)}
  }
}
