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
typealias SerialNumber = String

class AuthorizationPinDao: AtmDao.AuthorizationPin {
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

class MachineDao: AtmDao.Machine {
    fun getSerialNumber(serialNumber: SerialNumber) = AtmDb.Machine.Table.select {
        AtmDb.Machine.Table.serialNumber.eq(serialNumber)
    }.map {
        AtmDb.Machine.select(it)
    }.let {
        if (it.isEmpty()) throw Exception("No machine registered with serialNumber: serialNumber")
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

data class Reciept(val amount: Double? = null, val accountError: String = "", val machineError: String = "") {
    override fun toString() =
        if (amount == null) {
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

class LedgerService @Inject constructor(
    val machineDao: MachineDao,
    val ledgerDao: LedgerDao,
    val transactionDao: TransactionDao
    ) {

    fun withdraw(accountId: AccountId, amount: Amount): Reciept = transaction {
        //XXX - Needs to come from config. It hardcoded to match fixtures
        val machineLedger = machineDao.getSerialNumber("123456789")
        val customerLedger = ledgerDao.getByAccountId(accountId)

        var fees = 0
        var adjustedAmount = (amount / 20) * 20
        var reciept = Reciept()
        when {
            (machineLedger.balance < 20) -> {
                    reciept.copy(machineError = """Unable to process your withdrawal at this time.""")
                }
                amount > machineLedger.balance -> {
                    reciept.copy(machineError = """Unable to dispense full amount requested at this time""")
                    adjustedAmount = machineLedger.balance
                }
        }
        when  {
            //XXX - we need a machineDao so we can handle the following
            // Unable to dispense full amount requested at this time
            // Unable to process your withdrawal at this time.
            customerLedger.balance < 0 -> {
                reciept.copy(accountError = """Your account is overdrawn! You may not make withdrawals at this time.""")
            }
            adjustedAmount < customerLedger.balance -> {
                reciept.copy(amount = adjustedAmount)
            }
            adjustedAmount > customerLedger.balance -> {
                reciept.copy(
                    amount = adjustedAmount,
                    accountError = "You have been charged an overdraft fee of $5. Current balance: <balance>")
                fees += 5
            }
        }
        val totalAmount = amount + fees
        val updatedRecord = customerLedger.copy(balance = customerLedger.balance - totalAmount)
        ledgerDao.update(updatedRecord)

        val now = now()
        AtmDto.Transaction(-1, accountId, now, totalAmount, updatedRecord.balance).let {
            transactionDao.create(it)
            Reciept(it.amount)
        }
    }

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

    fun withdraw(token: Token, amount: Amount): Reciept {
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

