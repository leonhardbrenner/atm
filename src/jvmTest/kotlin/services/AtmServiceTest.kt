package services

import com.nhaarman.mockitokotlin2.*
import generated.model.AtmDto
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

interface View {
    fun displayItems(list: List<Element>)
    fun displayDetails(element: Element)
    fun displayError()
}

data class Element(val id: Int, val content: String)

interface DataProvider {
    fun getAll(): List<Element>
    fun getOne(id: Int): Element?
}

class Presenter(
    val view: View,
    val provider: DataProvider
) {
    fun start() {
        with(provider.getAll()) {
            if (this.isNotEmpty()) {
                view.displayItems(this)
            } else {
                view.displayError()
            }
        }
    }

    fun getOne(id: Int) {
        provider.getOne(id)?.let {
            view.displayDetails(it)
        }
    }
}

class PresenterTest {

    @Test
    fun `display non-empty list mockito-kotlin`() {
        val elements = listOf(
            Element(1, "first"),
            Element(2, "second")
        )

        val dataProvider: DataProvider = mock {
            on { getAll() } doReturn elements
        }

        val view: View = mock()

        val presenter = Presenter(view, dataProvider)

        presenter.start()

        Mockito.verify(view).displayItems(elements)
    }
}

//This is how I can mock final classes: https://antonioleiva.com/mockito-2-kotlin/
class AtmServiceTest {

    @Test
    fun `display non-empty list`() {
        DatabaseFactory.connect()
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
}
