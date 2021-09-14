package services

import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
import org.junit.Test
import utils.then
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

    @Test
    fun `AuthorizationService - verifyPin`() {
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
    fun `AuthorizationService - verifyToken`() {
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
        val result = service.verifyToken(token)
        verify(mockAuthorizationTokenDao).update(any())
        assertEquals(accountId, result)
    }


    @Test
    fun `AuthorizationService - endSession`() {
        val mockAuthorizationPinDao = mock<AuthorizationPinDao>()
        val mockAuthorizationTokenDao = mock<AuthorizationTokenDao>()
        val service = AuthorizationService(
            mockAuthorizationPinDao,
            mockAuthorizationTokenDao
        )
        service.endSession(token)
        verify(mockAuthorizationTokenDao).destroyByToken(any())
    }

    @Test
    fun `LedgerService - withdraw`() {
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = LedgerService(mockLedgerDao, mockTransactionDao)
        service.Account(accountId).withdraw(20.33)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - deposit`() {
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = LedgerService(mockLedgerDao, mockTransactionDao)
        service.Account(accountId).deposit(20.33)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - balance`() {
        val ledgerRecord = AtmDto.Ledger(123, accountId, 333.22)
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = LedgerService(mockLedgerDao, mockTransactionDao)
        val result = service.Account(accountId).balance
        verify(mockLedgerDao).getByAccountId(any())
        assertEquals(ledgerRecord.balance, result)
    }

    @Test
    fun `AtmService - login`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `AtmService - balance`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `AtmService - withdraw`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `AtmService - deposit`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `AtmService - history`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `AtmService - logout`() {
        TODO("Not implemented yet")
    }

}
