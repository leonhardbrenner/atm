import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.AccountId
import model.Response
import model.Token
import react.*
import react.dom.div

private val scope = MainScope()

const val initialDisplay = "What would you like to do?"
external interface AppState : RState {
    var accountId: AccountId?
    var token: Token?
    var response: Response?
    var display: String?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                accountId = null
                token = null
                response = null
                display = initialDisplay
            }
        }
    }

    fun handleInput(text: String) = text.split(' ').let { message ->
        val command = message.first()
        when (command) {
            "login" -> {
                scope.launch {
                    val receipt = Api.login(accountId = message[1], pin = message[2])
                    setState {
                        accountId = message[1]
                        token = receipt.token
                    }
                }
            }
            "balance" -> {
                scope.launch {
                    val receipt = Api.balance(accountId = state.accountId!!, token = state.token!!)
                    setState {
                        display = receipt.toString()
                    }
                }
            }
            "withdraw" -> {
                val amount = message[1]!!.toDouble()
                scope.launch {
                    val receipt = Api.withdraw(state.accountId!!, state.token!!, amount)
                    setState {
                        display = receipt.toString()
                    }
                }
            }
            "deposit" -> {
                val amount = message[1]!!.toDouble()
                scope.launch {
                    val receipt = Api.deposit(state.accountId!!, state.token!!, amount)
                    setState {
                        display = receipt.toString()
                    }
                }
            }
            "history" -> {
                scope.launch {
                    val receipt = Api.history(state.accountId!!, state.token!!)
                    setState {
                        display = receipt.toString()
                    }
                }
            }
            "logout" -> {
                scope.launch {
                    Api.logout(state.accountId!!, state.token!!)
                    setState {
                        accountId = null
                        token = null
                    }
                }
            }
            else -> throw Exception("Unknown command [$message]")
        }
    }

    override fun RBuilder.render() {
        div {
            + (state.display?:"")
            inputComponent {
                onSubmit = {
                    handleInput(it)
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
