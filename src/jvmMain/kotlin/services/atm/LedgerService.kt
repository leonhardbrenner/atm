package services.atm

import generated.dao.AtmDao
import generated.model.AtmDto
import generated.model.db.AtmDb
import model.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

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
                return@transaction Response(
                    machineError = "Unable to process your withdrawal at this time.")
            amount > machineLedger.balance ->
                "Unable to dispense full amount requested at this time"
            else -> null
        }

        val fee = when {
            customerLedger.balance < (adjustedAmount?:0.0) ->
                5.00
            else ->
                null
        }

        val accountError = when  {
            customerLedger.balance < 0.0 ->
                return@transaction Response(
                    accountError = "Your account is overdrawn! You may not make withdrawals at this time.")
            customerLedger.balance < (adjustedAmount?:0.0) ->
                "You have been charged an overdraft fee of $${fee}. Current balance: ${customerLedger.balance}"
            else ->
                null
        }

        val totalAmount = (adjustedAmount?:0.0) + (fee?:0.0)

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

const val SERIAL_NUMBER_HACK = "123456789" //XXX - this will only work for one machine
