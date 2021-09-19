package services

import Receipt
import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
import model.Response
import org.junit.Test
import kotlin.test.assertEquals

//This is how I can mock final classes: https://antonioleiva.com/mockito-2-kotlin/
class AtmServiceTest {
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
    fun `AuthorizationService - verifyPin - smoketest`() {
        val mockAuthorizationPinDao = mock<AuthorizationPinDao> {
            on { getByAccountId(any()) }.then {
                pinRecord
            }
        }
        val mockAuthorizationTokenDao = mock<AuthorizationTokenDao>()
        val service = AuthorizationService(
            mockAuthorizationPinDao,
            mockAuthorizationTokenDao
        )
        val token = service.verifyPin(accountId, pin)
        verify(mockAuthorizationTokenDao).create(any())
    }


    @Test
    fun `AuthorizationService - verifyToken - smoketest`() {
        val mockAuthorizationPinDao = mock<AuthorizationPinDao>()
        val mockAuthorizationTokenDao = mock<AuthorizationTokenDao> {
            on { getByToken(any()) }.then {
                AtmDto.AuthorizationToken(123, accountId, token, Long.MAX_VALUE) //Todo - get this closer to 2 minutes
            }
        }
        val service = AuthorizationService(
            mockAuthorizationPinDao,
            mockAuthorizationTokenDao
        )
        val result = service.verifyToken(accountId, token)
        verify(mockAuthorizationTokenDao).update(any())
        assertEquals(accountId, result)
    }


    @Test
    fun `AuthorizationService - endSession - smoketest`() {
        val mockAuthorizationPinDao = mock<AuthorizationPinDao>()
        val mockAuthorizationTokenDao = mock<AuthorizationTokenDao> {
            on { getByToken(any()) }.then {
                AtmDto.AuthorizationToken(2, accountId, token, Long.MAX_VALUE)
            }
        }
        val service = AuthorizationService(
            mockAuthorizationPinDao,
            mockAuthorizationTokenDao
        )
        service.endSession(accountId, token)
        verify(mockAuthorizationTokenDao).destroyByToken(any())
    }

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
    fun `LedgerService - withdraw - templete delete after other subtests are done`() {
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
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.00)
        val mockMachineDao = mock<MachineDao> {
            on { this.getBySerialNumber(machineRecord.serialNumber) }.then {
                machineRecord
            }
        }
        val service = LedgerService(mockMachineDao, mockLedgerDao, mockTransactionDao)
        val reciept = service.withdraw(accountId, 20.33)
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
        //XXX - Not implemented
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.0)
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
    fun `LedgerService - withdraw - not enough money`() {
        /**
         * The machine can’t dispense more money than it contains. If in the above two scenarios the machine contains less money than was
         * requested, the withdrawal amount should be adjusted to be the amount in the machine and this should be prepended to the return value:
         *      Unable to dispense full amount requested at this time.
         */
        //XXX - Not implemented
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.0)
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
    fun `LedgerService - withdraw - no money`() {
        /**
         * If instead there is no money in the machine, the return value should be this and only this:
         *      Unable to process your withdrawal at this time.
         */
        //XXX - Not implemented
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.0)
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
    fun `LedgerService - withdraw - already overdrawn`() {
        /**
         * If the account is already overdrawn, do not perform any checks against the available money in the machine, do not process the withdrawal,
         * and return only this:
         *      Your account is overdrawn! You may not make withdrawals at this time.
         */
        //XXX - Not implemented
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val machineRecord = AtmDto.Machine(123, serialNumber, 6660.0)
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

    @Test
    fun `AtmService - login - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>()
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.authorize(accountId, pin)
        verify(mockAuthorizationService).verifyPin(any(), any())
    }

    @Test
    fun `AtmService - balance - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any(), any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService> {
            on { balance(eq(accountId)) }.then {
                Response(
                    balance = ledgerRecord.balance
                )
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.balance(accountId, token)
        verify(mockAuthorizationService).verifyToken(any(), any())
        verify(mockLedgerService).balance(any())
    }

    @Test
    fun `AtmService - withdraw - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any(), any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.withdraw(accountId, token, amount)
        verify(mockAuthorizationService).verifyToken(any(), any())
        verify(mockLedgerService).withdraw(any(), any())
    }

    @Test
    fun `AtmService - deposit - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any(), any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.deposit(accountId, token, amount)
        verify(mockAuthorizationService).verifyToken(any(), any())
        verify(mockLedgerService).deposit(any(), any())
    }

    @Test
    fun `AtmService - history - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any(), any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.history(accountId, token)
        verify(mockAuthorizationService).verifyToken(any(), any())
        verify(mockTransactionDao).getByAccountId(any())
    }

    @Test
    fun `AtmService - logout - smoketest`() {
        val mockAuthorizationService = mock<AuthorizationService>()
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.logout(accountId, token)
        verify(mockAuthorizationService).endSession(any(), any())
    }

}
