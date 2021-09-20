package services

import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.db.AtmDb
import model.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

fun formatTimestamp(timestamp: Long) = with(Calendar.getInstance(Locale.ENGLISH)) {
    setTimeInMillis(timestamp)
    "%04d-%02d-%02d %02d:%02d:%02d".format(
        get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH),
        get(Calendar.HOUR), get(Calendar.MINUTE), get(Calendar.SECOND)
    )
}

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
    fun getBySerialNumber(serialNumber: SerialNumber) = AtmDb.Machine.Table.select {
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
    fun verifyToken(accountId: AccountId, token: Token): AccountId = transaction { //Todo - do this with token
        authorizationTokenDao.getByToken(token)?.let { result ->
            val now = now()
            if (now > result.expiration)
                throw Exception("Token has expired.")
            //Todo - update the expiration
            authorizationTokenDao.update(result.copy(expiration = now + lifespan))
            if (accountId != result.accountId)
                throw Exception("Authorization token does not belong to this account.")
            result.accountId
        }
    }

    /**
     * This should lookup the account token and make sure that it is not expired.
     */
    fun endSession(accountId: AccountId, token: Token) = transaction { //Todo - do this with token
        authorizationTokenDao.getByToken(token)?.let { result ->
            if (accountId != result.accountId)
                throw Exception("Authorization token does not belong to this account.")
            authorizationTokenDao.destroyByToken(token)
        }
    }
}

const val SERIAL_NUMBER_HACK = "123456789" //XXX - this will only work for one machine

class LedgerService @Inject constructor(
    val machineDao: MachineDao,
    val ledgerDao: LedgerDao,
    val transactionDao: TransactionDao
    ) {

    fun withdraw(accountId: AccountId, amount: Amount): Response = transaction {
        val machineLedger = machineDao.getBySerialNumber(SERIAL_NUMBER_HACK)
        val customerLedger = ledgerDao.getByAccountId(accountId)

        val adjustedAmount = ((amount / 20).toInt() * 20.0).let {
            when {
                machineLedger.balance < 20 ->
                    null
                amount > machineLedger.balance ->
                    machineLedger.balance
                else -> it
            }
        }

        val machineError = when {
            machineLedger.balance < 20 ->
                "Unable to process your withdrawal at this time."
            amount > machineLedger.balance ->
                "Unable to dispense full amount requested at this time"
            else -> null
        }

        val accountError = when  {
            customerLedger.balance < 0.0 ->
                "Your account is overdrawn! You may not make withdrawals at this time."
            customerLedger.balance < (adjustedAmount?:0.0) ->
                "You have been charged an overdraft fee of $5. Current balance: <balance>"
            else ->
                null
        }

        val fees = when {
            customerLedger.balance < (adjustedAmount?:0.0) ->
                5.00
            else ->
                null
        }

        val totalAmount = (adjustedAmount?:0.0) + (fees?:0.0)

        val newBalance = customerLedger.balance - totalAmount

        customerLedger.copy(balance = newBalance)
        ledgerDao.update(customerLedger.copy(balance = newBalance))
        machineDao.update(
            machineLedger.copy(balance = machineLedger.balance - (adjustedAmount?:0.0)))
        transactionDao.create(
            AtmDto.Transaction(-1, accountId, now(), -totalAmount, newBalance))
        Response(
            amount = adjustedAmount,
            balance = newBalance,
            accountError = accountError,
            machineError = machineError
        )
    }

    fun deposit(accountId: AccountId, amount: Amount): Response = transaction {
        val record = ledgerDao.getByAccountId(accountId)
        val newBalance = record.balance + amount
        val updatedRecord = record.copy(balance = newBalance)
        ledgerDao.update(updatedRecord)
        transactionDao.create(
            AtmDto.Transaction(-1, accountId, now(), amount, newBalance))
        Response(
            balance = newBalance
        )
    }

    fun balance(accountId: AccountId): Response = transaction {
        Response(balance = ledgerDao.getByAccountId(accountId).balance)
    }
}

fun now() = System.currentTimeMillis()

class AtmService @Inject constructor(
    val authorizationService: AuthorizationService,
    val ledgerService: LedgerService,
    val transactionDao: TransactionDao
) {

    fun authorize(accountId: AccountId, pin: Pin): Response =
        try {
            Response(token = authorizationService.verifyPin(accountId, pin))
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }

    fun logout(accountId: AccountId, token: Token) = transaction {
        authorizationService.endSession(accountId, token)
    }

    fun balance(accountId: AccountId, token: Token): Response =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.balance(accountId)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }

    fun withdraw(accountId: AccountId, token: Token, amount: Amount) =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.withdraw(accountId, amount)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }


    fun deposit(accountId: AccountId, token: Token, amount: Amount) =
        try {
            authorizationService.verifyToken(accountId, token)
            ledgerService.deposit(accountId, amount)
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }


    fun history(accountId: AccountId, token: Token) =
        try {
            authorizationService.verifyToken(accountId, token)
            transaction { //Todo - Move to a service
                Response(
                    history = transactionDao.getByAccountId(accountId).map {
                        Transaction(it, formatTimestamp(it.timestamp))
                    }
                )
            }
        } catch (ex: Exception) {
            Response(authorizationError = ex.message!!)
        }


}