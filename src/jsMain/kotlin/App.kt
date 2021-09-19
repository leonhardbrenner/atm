import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.AccountId
import model.Reciept
import model.Token
import react.*
import react.dom.div

private val scope = MainScope()

const val initialDisplay = "What would you like to do?"
external interface AppState : RState {
    var accountId: AccountId?
    var token: Token?
    var reciept: Reciept?
    var display: String?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                accountId = null
                token = null
                reciept = null
                display = initialDisplay
            }
        }
    }

    fun handleInput(text: String) = text.split(' ').let { message ->
        val command = message.first()
        when (command) {
            "login" -> {
                scope.launch {
                    val newToken = Api.login(accountId = message[1], pin = message[2]).token
                    setState {
                        accountId = message[1]
                        token = newToken
                    }
                }
            }
            //"balance" -> {
            //    scope.launch { Api.balance(accountId!!, token!!) }
            //}
            //"withdraw" -> {
            //    val amount = message[1]!!.toDouble()
            //    scope.launch { Api.withdraw(accountId!!, token!!, amount) }
            //}
            //"deposit" -> {
            //    val amount = message[1]!!.toDouble()
            //    scope.launch { Api.deposit(accountId!!, token!!, amount) }
            //}
            //"history" -> {
            //    scope.launch { Api.history(accountId!!, token!!) }
            //}
            else -> throw Exception("Unknown command [$message]")
        }
    }

    override fun RBuilder.render() {
        div {
            + (state.display?:"")
            + (state.token?:"")
            inputComponent {
                onSubmit = {
                    handleInput(it)
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
