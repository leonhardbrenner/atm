package generated.model

import kotlin.Double
import kotlin.Int
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
    var expiration: String?
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
}
