package services

import com.authzee.kotlinguice4.getInstance
import com.google.inject.AbstractModule
import com.google.inject.Guice
import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.db.AtmDb
import model.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import javax.inject.Inject

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

    fun withdrawOld(accountId: AccountId, amount: Amount): Response = transaction {
        //XXX - Needs to come from config. It hardcoded to match fixtures
        val machineLedger = machineDao.getBySerialNumber(SERIAL_NUMBER_HACK)
        val customerLedger = ledgerDao.getByAccountId(accountId)

        var fees = 0
        var adjustedAmount = (amount / 20) * 20
        var response = Response()
        when {
            (machineLedger.balance < 20) -> {
                    response.copy(machineError = """Unable to process your withdrawal at this time.""")
                }
                amount > machineLedger.balance -> {
                    response.copy(machineError = """Unable to dispense full amount requested at this time""")
                    adjustedAmount = machineLedger.balance
                }
        }
        when  {
            //XXX - we need a machineDao so we can handle the following
            // Unable to dispense full amount requested at this time
            // Unable to process your withdrawal at this time.
            customerLedger.balance < 0 -> {
                response.copy(accountError = """Your account is overdrawn! You may not make withdrawals at this time.""")
            }
            adjustedAmount < customerLedger.balance -> {
                response.copy(amount = adjustedAmount)
            }
            adjustedAmount > customerLedger.balance -> {
                response.copy(
                    amount = adjustedAmount,
                    accountError = "You have been charged an overdraft fee of $5. Current balance: <balance>")
                fees += 5
            }
        }
        val totalAmount = amount + fees
        val updatedRecord = customerLedger.copy(balance = customerLedger.balance - totalAmount)
        ledgerDao.update(updatedRecord)

        val now = now()
        AtmDto.Transaction(-1, accountId, now, totalAmount).let {
            transactionDao.create(it)
            Response(it.amount, updatedRecord.balance)
        }
    }

    inner class Withdraw(val accountId: AccountId, val amount: Amount) {
        val machineLedger by lazy { machineDao.getBySerialNumber(SERIAL_NUMBER_HACK) }
        val customerLedger by lazy { ledgerDao.getByAccountId(accountId) }
        var machineError: String? = null

        val adjustedAmount by lazy {
            ((amount / 20).toInt() * 20.0).let {
                if (amount > machineLedger.balance) {
                    machineError = """Unable to dispense full amount requested at this time"""
                    machineLedger.balance
               } else
                    it
            }
        }

        val response get() = transaction {
            var fees = 0

            var response = Response()
            if (machineLedger.balance < 20)
                machineError = """Unable to process your withdrawal at this time."""

            when  {
                //XXX - we need a machineDao so we can handle the following
                // Unable to dispense full amount requested at this time
                // Unable to process your withdrawal at this time.
                customerLedger.balance < 0 -> {
                    response.copy(accountError = """Your account is overdrawn! You may not make withdrawals at this time.""")
                }
                adjustedAmount < customerLedger.balance -> {
                    response.copy(amount = adjustedAmount)
                }
                adjustedAmount > customerLedger.balance -> {
                    response.copy(
                        amount = adjustedAmount,
                        accountError = "You have been charged an overdraft fee of $5. Current balance: <balance>")
                    fees += 5
                }
            }
            response.copy(machineError = machineError)
            val totalAmount = adjustedAmount + fees
            val updatedRecord = customerLedger.copy(balance = customerLedger.balance - totalAmount)
            ledgerDao.update(updatedRecord)

            val now = now()
            AtmDto.Transaction(-1, accountId, now, totalAmount).let {
                transactionDao.create(it)
                Response(it.amount, updatedRecord.balance)
            }

        }

    }
    fun withdraw(accountId: AccountId, amount: Amount): Response = Withdraw(accountId, amount).response

    fun deposit(accountId: AccountId, amount: Amount): Response = transaction {
        val record = ledgerDao.getByAccountId(accountId)
        val updatedRecord = record.copy(balance = record.balance + amount)
        ledgerDao.update(updatedRecord)
        val now = now()
        AtmDto.Transaction(-1, accountId, now, amount).let {
            transactionDao.create(it)
            Response(it.amount, updatedRecord.balance)
        }
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

    fun authorize(accountId: AccountId, pin: Pin) =
        Response().let {
            try {
                it.copy(token = authorizationService.verifyPin(accountId, pin))
            } catch (ex: Exception) {
                it.copy(accountError = ex.message!!)
            }
        }

    fun logout(accountId: AccountId, token: Token) = transaction {
        authorizationService.endSession(accountId, token)
    }

    fun balance(accountId: AccountId, token: Token): Response {
        authorizationService.verifyToken(accountId, token) //Todo - move this to AtmSession
        return ledgerService.balance(accountId)
    }

    fun withdraw(accountId: AccountId, token: Token, amount: Amount) =
        authorizationService.verifyToken(accountId, token)?.let { accountId ->
            ledgerService.withdraw(accountId, amount)
        }

    fun deposit(accountId: AccountId, token: Token, amount: Amount) =
        authorizationService.verifyToken(accountId, token)?.let { accountId ->
            ledgerService.deposit(accountId, amount)
        }

    fun history(accountId: AccountId, token: Token) =
        authorizationService.verifyToken(accountId, token).let { accountId ->
            //Todo - make a transaction service and move this there.
            Response(
                history = transaction { transactionDao.getByAccountId(accountId) } //Todo - Move to a service
            )
        }

}

