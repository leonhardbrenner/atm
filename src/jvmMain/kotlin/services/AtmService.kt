package services

import com.authzee.kotlinguice4.getInstance
import com.google.inject.AbstractModule
import com.google.inject.Guice
import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.db.AtmDb
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

typealias AccountId = String
typealias Amount = Double
typealias Pin = String
typealias Token = String

class AuthorizationPinDao: AtmDao.AuthorizationPin() {
    fun get(accountId: AccountId) = AtmDb.AuthorizationPin.Table.select {
        AtmDb.AuthorizationPin.Table.accountId.eq(accountId)
    }.map {
        AtmDb.AuthorizationPin.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Account not found")
        it.last()
    }
}

class AuthorizationTokenDao: AtmDao.AuthorizationToken() {
    fun get(token: Token) = AtmDb.AuthorizationToken.Table.select {
        AtmDb.AuthorizationToken.Table.token.eq(token)
    }.map {
        AtmDb.AuthorizationToken.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Token not found")
        it.last()
    }

    fun destroy(token: Token) = AtmDb.AuthorizationToken.Table.deleteWhere {
        AtmDb.AuthorizationToken.Table.token eq token }
}

class LedgerDao: AtmDao.Ledger() {
    fun get(token: Token) = AtmDb.Ledger.Table.select {
        AtmDb.Ledger.Table.accountId.eq(token)
    }.map {
        AtmDb.Ledger.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("No ledger for account")
        it.last()
    }
}

fun createToken() = UUID.randomUUID().toString()

const val lifespan = 120000

class AuthorizationService @Inject constructor(
    val authorizationPinDao: AuthorizationPinDao,
    val authorizationTokenDao: AuthorizationTokenDao
) {
    /**
     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
     */
    fun verifyPin(accountId: AccountId, pin: Pin): Token = transaction {
        if (pin == authorizationPinDao.get(accountId).pin) { //Todo - hash(pin)
            val token = createToken()
            val now = System.currentTimeMillis()
            authorizationTokenDao.create(AtmDto.AuthorizationToken(-1, accountId, token, now + lifespan))
            token
        } else {
            throw Exception("Invalid Pin")
        }
    }

    /**
     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
     */
    fun verifyToken(token: Token): AccountId = transaction { //Todo - do this with token
        authorizationTokenDao.get(token)?.let { result ->
            val now = System.currentTimeMillis()
            if (System.currentTimeMillis() > result.expiration)
                throw Exception("Token has expired.")
            //Todo - update the expiration
            authorizationTokenDao.update(result.copy(expiration = now + lifespan))
            result.accountId
        }
    }

    /**
     * This should lookup the account token and make sure that it is not expired.
     */
    fun endSession(token: Token) = transaction { //Todo - do this with token
        authorizationTokenDao.destroy(token)
    }
}

data class WithdrawResponse(val amount: Amount, val balance: Amount)
data class DepositResponse(val amount: Amount, val balance: Amount)

class LedgerService @Inject constructor(val ledgerDao: LedgerDao) {

    inner class Account(val accountId: AccountId) {

        fun withdraw(amount: Amount): WithdrawResponse = transaction {
            val record = ledgerDao.get(accountId)
            if (amount > record.balance)
                throw Exception("Funds are not available.")
            val updatedRecord = record.copy(balance = record.balance - amount)
            ledgerDao.update(updatedRecord)
            WithdrawResponse(amount, updatedRecord.balance)
        }

        fun deposit(amount: Amount): DepositResponse = transaction {
            val record = ledgerDao.get(accountId)
            record.copy(balance = record.balance + amount).let {
                ledgerDao.update(it)
                DepositResponse(amount, it.balance)
            }
        }

        val balance get(): Double = transaction { ledgerDao.get(2).balance }
    }
}

class Atm @Inject constructor(
    val authorizationService: AuthorizationService,
    val ledgerService: LedgerService
) {

    object Module : AbstractModule() {
        override fun configure() {
            //bind(CoroutineDatabase::class.java).toInstance(database())
        }

    }

    data class Result(val message: String, val amount: Amount)

    fun login(accountId: AccountId, pin: Pin) =
        authorizationService.verifyPin(accountId, pin)

    fun balance(token: Token): Double {
        val accountId = authorizationService.verifyToken(token)
        return ledgerService.Account(accountId).balance
    }

    /**
     * Removes value from the authorized account. The machine only contains $20 bills, so the withdrawal amount must be a multiple of 20.
     * withdraw <value>
     *
     * If account has not been overdrawn, returns balance after withdrawal in the format:
     *      Amount dispensed: $<x>
     *      Current balance: <balance>
     *
     * If the account has been overdrawn with this transaction, removes a further $5 from their account, and returns:
     *      Amount dispensed: $<x>
     *      You have been charged an overdraft fee of $5. Current balance: <balance>
     *
     * The machine can’t dispense more money than it contains. If in the above two scenarios the machine contains less money than was
     * requested, the withdrawal amount should be adjusted to be the amount in the machine and this should be prepended to the return value:
     *      Unable to dispense full amount requested at this time.
     *
     * If instead there is no money in the machine, the return value should be this and only this:
     *      Unable to process your withdrawal at this time.
     *
     * If the account is already overdrawn, do not perform any checks against the available money in the machine, do not process the withdrawal,
     * and return only this:
     *      Your account is overdrawn! You may not make withdrawals at this time.
     *
     */
    fun withdraw(token: Token, amount: Amount): WithdrawResponse {
        val accountId = authorizationService.verifyToken(token)
        return ledgerService.Account(accountId).withdraw(amount)
    }

    /**
     * Adds value to the authorized account. The deposited amount does not need to be a multiple of 20.
     *      deposit <value>
     * Returns the account’s balance after deposit is made in the format:
     *      Current balance: <balance>
     */
    fun deposit(token: Token, amount: Amount): DepositResponse {
        val accountId = authorizationService.verifyToken(token)
        return ledgerService.Account(accountId).deposit(amount)
    }

    fun history(token: Token) {
        val accountId = authorizationService.verifyToken(token)

    }

    fun logout(token: Token) {
        val accountId = authorizationService.endSession(token)
    }

}

fun main(args:Array<String>) {
    DatabaseFactory.init()
    val atm = Guice.createInjector(Atm.Module).getInstance<Atm>()
    val accountId = "1434597300"
    val pin = "4557"
    atm.login(accountId, pin).let { token ->
        println(token)
        //println(atm.balance(token))
        //println(atm.balance("Invalid Token"))
        println(atm.balance(token))
        println(atm.withdraw(token, 22.33))
        println(atm.deposit(token, 200.00))
        atm.history(token)
        atm.logout(accountId)
        //atm.authorizationService.verifyToken(accountId, token) //This should not be available

    }
}

