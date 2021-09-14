package services

import com.authzee.kotlinguice4.getInstance
import com.google.inject.AbstractModule
import com.google.inject.Guice
import generated.dao.AtmDao
import generated.model.Atm
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

class AuthorizationPinDao: AtmDao.AuthorizationPin { //TODO - Make this and base interfaces
    fun getByAccountId(accountId: AccountId) = AtmDb.AuthorizationPin.Table.select {
        AtmDb.AuthorizationPin.Table.accountId.eq(accountId)
    }.map {
        AtmDb.AuthorizationPin.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Account not found")
        it.last()
    }
}

class AuthorizationTokenDao: AtmDao.AuthorizationToken {
    fun getByToken(token: Token) = AtmDb.AuthorizationToken.Table.select {
        AtmDb.AuthorizationToken.Table.token.eq(token)
    }.map {
        AtmDb.AuthorizationToken.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("Token not found")
        it.last()
    }

    fun destroyByToken(token: Token) = AtmDb.AuthorizationToken.Table.deleteWhere {
        AtmDb.AuthorizationToken.Table.token eq token }
}

class LedgerDao: AtmDao.Ledger {
    fun getByAccountId(accountId: AccountId) = AtmDb.Ledger.Table.select {
        AtmDb.Ledger.Table.accountId.eq(accountId)
    }.map {
        AtmDb.Ledger.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("No ledger for account")
        it.last()
    }
}

class TransactionDao: AtmDao.Transaction {
    fun getByAccountId(accountId: AccountId) = AtmDb.Transaction.Table.select {
        AtmDb.Transaction.Table.accountId.eq(accountId)
    }.map {
        AtmDb.Transaction.select(it)
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
        if (pin == authorizationPinDao.getByAccountId(accountId).pin) { //Todo - hash(pin)
            val token = createToken()
            authorizationTokenDao.create(AtmDto.AuthorizationToken(-1, accountId, token, now() + lifespan))
            token
        } else {
            throw Exception("Invalid Pin")
        }
    }

    /**
     * This should lookup the account hashed_pin and compare against hash(pin) and return a token
     */
    fun verifyToken(token: Token): AccountId = transaction { //Todo - do this with token
        authorizationTokenDao.getByToken(token)?.let { result ->
            val now = now()
            if (now > result.expiration)
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
        authorizationTokenDao.destroyByToken(token)
    }
}

class LedgerService @Inject constructor(
    val ledgerDao: LedgerDao,
    val transactionDao: TransactionDao
    ) {

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
    fun withdraw(accountId: AccountId, amount: Amount): AtmDto.Transaction = transaction {
        val record = ledgerDao.getByAccountId(accountId)
        if (amount > record.balance)
            throw Exception("Funds are not available.")
        //Todo - handle exceptions better. We can extend Exception and build message as a format in toString()
        val updatedRecord = record.copy(balance = record.balance - amount)
        ledgerDao.update(updatedRecord)
        val now = now()
        AtmDto.Transaction(-1, accountId, now, amount, updatedRecord.balance).apply {
            transactionDao.create(this)
        }
    }

    /**
     * Adds value to the authorized account. The deposited amount does not need to be a multiple of 20.
     *      deposit <value>
     * Returns the account’s balance after deposit is made in the format:
     *      Current balance: <balance>
     */
    fun deposit(accountId: AccountId, amount: Amount): AtmDto.Transaction = transaction {
        val record = ledgerDao.getByAccountId(accountId)
        val updatedRecord = record.copy(balance = record.balance + amount)
        ledgerDao.update(updatedRecord)
        val now = now()
        AtmDto.Transaction(-1, accountId, now, amount, updatedRecord.balance).apply {
            transactionDao.create(this)
        }
    }

    fun balance(accountId: AccountId): Double = transaction {
        ledgerDao.getByAccountId(accountId).balance
    }
}

fun now() = System.currentTimeMillis()

class AtmService @Inject constructor(
    val authorizationService: AuthorizationService,
    val ledgerService: LedgerService,
    val transactionDao: TransactionDao
) {

    fun login(accountId: AccountId, pin: Pin) =
        authorizationService.verifyPin(accountId, pin)

    fun balance(token: Token): Double {
        val accountId = authorizationService.verifyToken(token) //Todo - move this to AtmSession
        return ledgerService.balance(accountId)
    }

    fun withdraw(token: Token, amount: Amount): Atm.Transaction {
        val accountId = authorizationService.verifyToken(token)
        return ledgerService.withdraw(accountId, amount)
    }

    fun deposit(token: Token, amount: Amount): Atm.Transaction {
        val accountId = authorizationService.verifyToken(token)
        return ledgerService.deposit(accountId, amount)
    }

    fun history(token: Token) = transaction {
        val accountId = authorizationService.verifyToken(token)
        transactionDao.getByAccountId(accountId) //Todo - Get by accountId
    }

    fun logout(token: Token) = transaction {
        authorizationService.endSession(token)
    }

}

class AtmSession @Inject constructor(
    val atmService: AtmService
) {
    var token: Token? = null

    object Module : AbstractModule() {
        override fun configure() {
            //bind(CoroutineDatabase::class.java).toInstance(database())
        }
    }

    fun handleMessage(message: String) = message.split(' ').let { message ->
        val command = message.first()
        when (command) {
            "login" -> {
                val accountId = message[1]!!
                val pin = message[2]!!
                token = atmService.login(accountId, pin)
                token
            }
            "balance" -> {
                atmService.balance(token!!)
            }
            "withdraw" -> {
                val amount = message[1]!!.toDouble()
                atmService.withdraw(token!!, amount)
            }
            "deposit" -> {
                val amount = message[1]!!.toDouble()
                atmService.deposit(token!!, amount)
            }
            "history" -> {
                atmService.history(token!!)
            }
            "logout" -> {
                atmService.logout(token!!)
                token = null
            }
            else -> throw Exception("Unknown command [$message]")
        }
    }
}


fun main(args:Array<String>) {
    DatabaseFactory.init()
    val atm = Guice.createInjector(AtmSession.Module).getInstance<AtmSession>()

    val accountId = "1434597300"
    val pin = "4557"
    val commands = listOf(
        "login $accountId $pin",
        "balance",
        "withdraw 22.33",
        "deposit 200.00",
        "history",
        "logout"
    )
    commands.forEach { message ->
        println(atm.handleMessage(message))
    }
    try {
        atm.handleMessage("balance")
    } catch (ex: Exception) {
        print("Expected failure")
    }
}

