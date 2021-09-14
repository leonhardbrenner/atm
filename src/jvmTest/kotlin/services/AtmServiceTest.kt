package services

import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
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
        service.withdraw(accountId, 20.33)
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
        service.deposit(accountId, 20.33)
        verify(mockLedgerDao).getByAccountId(any())
        verify(mockLedgerDao).update(any())
        verify(mockTransactionDao).create(any())
    }

    @Test
    fun `LedgerService - balance`() {
        val mockLedgerDao = mock<LedgerDao>() {
            on { getByAccountId(any()) }.then {
                ledgerRecord
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = LedgerService(mockLedgerDao, mockTransactionDao)
        val result = service.balance(accountId)
        verify(mockLedgerDao).getByAccountId(any())
        assertEquals(ledgerRecord.balance, result)
    }

    @Test
    fun `AtmService - login`() {
        val mockAuthorizationService = mock<AuthorizationService>()
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.login(accountId, pin)
        verify(mockAuthorizationService).verifyPin(any(), any())
    }

    @Test
    fun `AtmService - balance`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService> {
            on { balance(eq(accountId)) }.then {
                ledgerRecord.balance
            }
        }
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.balance(token)
        verify(mockAuthorizationService).verifyToken(any())
        verify(mockLedgerService).balance(any())
    }

    @Test
    fun `AtmService - withdraw`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.withdraw(token, amount)
        verify(mockAuthorizationService).verifyToken(any())
        verify(mockLedgerService).withdraw(any(), any())
    }

    @Test
    fun `AtmService - deposit`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.deposit(token, amount)
        verify(mockAuthorizationService).verifyToken(any())
        verify(mockLedgerService).deposit(any(), any())
    }

    @Test
    fun `AtmService - history`() {
        val mockAuthorizationService = mock<AuthorizationService>() {
            on { verifyToken(any()) }.then {
                accountId
            }
        }
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.history(token)
        verify(mockAuthorizationService).verifyToken(any())
        verify(mockTransactionDao).getByAccountId(any())
    }

    @Test
    fun `AtmService - logout`() {
        val mockAuthorizationService = mock<AuthorizationService>()
        val mockLedgerService = mock<LedgerService>()
        val mockTransactionDao = mock<TransactionDao>()
        val service = AtmService(mockAuthorizationService, mockLedgerService, mockTransactionDao)
        service.logout(token)
        verify(mockAuthorizationService).endSession(any())
    }

    @Test fun `AtmSession - cli - login`(){
        val mockAtmService = mock<AtmService>()
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        verify(mockAtmService).login(eq(accountId), eq(pin))
    }

    val mockAtmService = mock<AtmService> {
        on { login(accountId, pin) }.then {
            token
        }
    }

    @Test fun `AtmSession - cli - balance`(){
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        cli.handleMessage("balance")
        verify(mockAtmService).balance(eq(token))
    }

    @Test fun `AtmSession - cli - withdraw`(){
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        cli.handleMessage("withdraw $amount")
        verify(mockAtmService).withdraw(eq(token), eq(amount))
    }

    @Test fun `AtmSession - cli - deposit`(){
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        cli.handleMessage("deposit $amount")
        verify(mockAtmService).deposit(eq(token), eq(amount))
    }

    @Test fun `AtmSession - cli - history`(){
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        cli.handleMessage("history")
        verify(mockAtmService).history(eq(token))
    }

    @Test fun `AtmSession - cli - logout`(){
        val cli = AtmSession(mockAtmService)
        cli.handleMessage("login ${accountId} ${pin}")
        cli.handleMessage("logout")
        verify(mockAtmService).logout(eq(token))
        assertEquals(null, cli.token)
    }
}
