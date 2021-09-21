package services.atm

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import generated.model.AtmDto
import org.junit.Test
import kotlin.test.assertEquals

class LedgerServiceTest {
    init {
        DatabaseFactory.connect() //This is because transaction will blow up.
    }
    val accountId = "123456"
    val pin = "4321"
    val pinRecord = AtmDto.AuthorizationPin(1, accountId, pin)
    val token = "XYZ"
    val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
    val amount = 99.99
    val serialNumber = SERIAL_NUMBER_HACK

    @Test
    fun `LedgerService - withdraw - smoketest`() {
        /**
         * If account has not been overdrawn, returns balance after withdrawal in the format:
         *      Amount dispensed: $<x>
         *      Current balance: <balance>
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6000.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        service.withdraw(accountId, 20.33)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - withdraw - overdrawn with this transaction`() {
        /**
         * If the account has been overdrawn with this transaction, removes a further $5 from their account, and returns:
         *      Amount dispensed: $<x>
         *      You have been charged an overdraft fee of $5. Current balance: <balance>
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, 33.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val response = service.withdraw(accountId, 40.0)
        val accountError = "You have been charged an overdraft fee of \$5.0. Current balance: 33.22"
        assertEquals(accountError, response.accountError)
        assertEquals(40.0, response.amount)
        assertEquals(-11.780000000000001, response.balance)
        assertEquals(null, response.token)
        assertEquals(null, response.history)
        assertEquals(null, response.token)
        assertEquals(null, response.authorizationError)
        assertEquals(null, response.machineError)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - withdraw - not enough money`() {
        /**
         * The machine can’t dispense more money than it contains. If in the above two scenarios the machine contains less money than was
         * requested, the withdrawal amount should be adjusted to be the amount in the machine and this should be prepended to the return value:
         *      Unable to dispense full amount requested at this time.
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 60.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val response = service.withdraw(accountId, 100.0)
        assertEquals(null, response.accountError)
        assertEquals(60.0, response.amount)
        assertEquals(273.22, response.balance)
        assertEquals(null, response.token)
        assertEquals(null, response.history)
        assertEquals(null, response.token)
        assertEquals(null, response.authorizationError)
        assertEquals("Unable to dispense full amount requested at this time", response.machineError)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - withdraw - no money`() {
        /**
         * If instead there is no money in the machine, the return value should be this and only this:
         *      Unable to process your withdrawal at this time.
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 0.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val response = service.withdraw(accountId, 100.0)
        assertEquals(null, response.accountError)
        assertEquals(null, response.amount)
        assertEquals(null, response.balance)
        assertEquals(null, response.token)
        assertEquals(null, response.history)
        assertEquals(null, response.token)
        assertEquals(null, response.authorizationError)
        assertEquals("Unable to process your withdrawal at this time.", response.machineError)
        verify(mockLedgerDao).getByAccountId(any())
        //verify(mockLedgerDao).update(any())
        //verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - withdraw - already overdrawn`() {
        /**
         * If the account is already overdrawn, do not perform any checks against the available money in the machine, do not process the withdrawal,
         * and return only this:
         *      Your account is overdrawn! You may not make withdrawals at this time.
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, -10.0) //Todo - 0 did not work but is also not consiedered overdrawn.
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 10000.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val response = service.withdraw(accountId, 100.0)
        assertEquals("Your account is overdrawn! You may not make withdrawals at this time.", response.accountError)
        assertEquals(null, response.amount)
        assertEquals(null, response.balance)
        assertEquals(null, response.token)
        assertEquals(null, response.history)
        assertEquals(null, response.token)
        assertEquals(null, response.authorizationError)
        assertEquals(null, response.machineError)
        verify(mockLedgerDao).getByAccountId(any())
        //verify(mockLedgerDao).update(any())
        //verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - deposit - smoketest`() {
        /**
         * Adds value to the authorized account. The deposited amount does not need to be a multiple of 20.
         *      deposit <value>
         * Returns the account’s balance after deposit is made in the format:
         *      Current balance: <balance>
         */
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val mockMachineDao = mock<MachineDao>()
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        service.deposit(accountId, 20.33)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - balance - smoketest`() {
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val mockMachineDao = mock<MachineDao>()
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val result = service.balance(accountId)
        verify(mockLedgerDao).getByAccountId(any())
        assertEquals(ledgerRecord.balance, result.balance)
    }

}