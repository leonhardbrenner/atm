package services.atm

import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
import org.junit.Test
import kotlin.test.assertEquals

class AuthorizationServiceTest {

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

}
