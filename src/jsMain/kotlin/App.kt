import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.WhiteSpace
import kotlinx.css.br
import kotlinx.css.p
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

//XXX - Amount dispensed <=> deposited
fun Response.display() =
    """
        ${amount?.let { "Amount dispensed: $it" }?: ""} 
        ${balance?.let { "Current balance: $it" }?: ""} 
        ${history?.let { "History:" + it.joinToString(", ") + ""}?: ""}
        ${accountError?.let { "Account Error: $it" }?: ""} 
        ${machineError?.let { "Machine Error: $it" }?: ""} 
""".trimIndent()

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
        if (state.token == null && !listOf("authorize", "logout", "end").contains(command)) { //Todo -> enum
            setState {
                display = "Authorization required."
            }
        } else {
            when (command) {
                "authorize" -> {
                    scope.launch {
                        val response = Api.authorize(accountId = message[1], pin = message[2])
                        setState {
                            accountId = message[1]
                            token = response.token
                        }
                        setState {
                            display = if (response.token != null)
                                "${state.accountId} successfully authorized."
                            else
                                "Authorization failed."
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
                "balance" -> {
                    scope.launch {
                        val response = Api.balance(accountId = state.accountId!!, token = state.token!!)
                        setState {
                            display = response.display()
                        }
                    }
                }
                "withdraw" -> {
                    val amount = message[1]!!.toDouble()
                    scope.launch {
                        val response = Api.withdraw(state.accountId!!, state.token!!, amount)
                        setState {
                            display = response.display()
                        }
                    }
                }
                "deposit" -> {
                    val amount = message[1]!!.toDouble()
                    scope.launch {
                        val response = Api.deposit(state.accountId!!, state.token!!, amount)
                        setState {
                            display = response.display()
                        }
                    }
                }
                "history" -> {
                    scope.launch {
                        val response = Api.history(state.accountId!!, state.token!!)
                        setState {
                            display = response.display()
                        }
                    }
                }
                else -> throw Exception("Unknown command [$message]")
            }
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
