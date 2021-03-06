package generated.model

import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String

interface AtmBuilder {
  class AuthorizationPin(
    var id: Int?,
    var accountId: String?,
    var pin: String?
  ) {
    fun build(): Atm.AuthorizationPin = AtmDto.AuthorizationPin(
    id ?: throw IllegalArgumentException("id is not nullable"),
    accountId ?: throw IllegalArgumentException("accountId is not nullable"),
    pin ?: throw IllegalArgumentException("pin is not nullable")
    )}

  class AuthorizationToken(
    var id: Int?,
    var accountId: String?,
    var token: String?,
    var expiration: Long?
  ) {
    fun build(): Atm.AuthorizationToken = AtmDto.AuthorizationToken(
    id ?: throw IllegalArgumentException("id is not nullable"),
    accountId ?: throw IllegalArgumentException("accountId is not nullable"),
    token ?: throw IllegalArgumentException("token is not nullable"),
    expiration ?: throw IllegalArgumentException("expiration is not nullable")
    )}

  class Ledger(
    var id: Int?,
    var accountId: String?,
    var balance: Double?
  ) {
    fun build(): Atm.Ledger = AtmDto.Ledger(
    id ?: throw IllegalArgumentException("id is not nullable"),
    accountId ?: throw IllegalArgumentException("accountId is not nullable"),
    balance ?: throw IllegalArgumentException("balance is not nullable")
    )}

  class Machine(
    var id: Int?,
    var serialNumber: String?,
    var balance: Double?
  ) {
    fun build(): Atm.Machine = AtmDto.Machine(
    id ?: throw IllegalArgumentException("id is not nullable"),
    serialNumber ?: throw IllegalArgumentException("serialNumber is not nullable"),
    balance ?: throw IllegalArgumentException("balance is not nullable")
    )}

  class Transaction(
    var id: Int?,
    var accountId: String?,
    var timestamp: Long?,
    var amount: Double?,
    var balance: Double?
  ) {
    fun build(): Atm.Transaction = AtmDto.Transaction(
    id ?: throw IllegalArgumentException("id is not nullable"),
    accountId ?: throw IllegalArgumentException("accountId is not nullable"),
    timestamp ?: throw IllegalArgumentException("timestamp is not nullable"),
    amount ?: throw IllegalArgumentException("amount is not nullable"),
    balance ?: throw IllegalArgumentException("balance is not nullable")
    )}
}
