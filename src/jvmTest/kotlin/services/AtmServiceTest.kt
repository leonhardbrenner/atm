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

    @Test
    fun `AuthorizationService - verifyPin`() {
        val accountId = "123456"
        val pin = "4321"
        val pinRecord = AtmDto.AuthorizationPin(1, accountId, pin)
        val mockAuthorizationPinDao = mock<AuthorizationPinDao> {
            on { getByAccountId(any()) }.then {
                pinRecord
            }
        }
        val mockAuthorizationTokenDao = mock<AuthorizationTokenDao>()
        val authorizationService = AuthorizationService(
            mockAuthorizationPinDao,
            mockAuthorizationTokenDao
        )
        assertEquals(pinRecord, mockAuthorizationPinDao.getByAccountId(accountId))
        val token = authorizationService.verifyPin(accountId, pin)
        verify(mockAuthorizationTokenDao).create(any())
    }


    @Test
    fun `AuthorizationService - verifyToken`() {
        TODO("Not implemented yet")
    }


    @Test
    fun `AuthorizationService - endSession`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `LedgerService - withdraw`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `LedgerService - deposit`() {
        TODO("Not implemented yet")
    }

    @Test
    fun `LedgerService - balance`() {
        TODO("Not implemented yet")
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
