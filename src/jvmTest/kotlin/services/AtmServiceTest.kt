package services

import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
import model.Response
import org.junit.Test
import services.atm.*

//Note: This is how I mock final classes: https://antonioleiva.com/mockito-2-kotlin/
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
