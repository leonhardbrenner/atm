import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.AccountId
import model.Response
import model.Token
import react.*
import react.dom.*

private val scope = MainScope()

external interface AppState : RState {
    var accountId: AccountId?
    var token: Token?
    var authorizationMessage: String?
    var response: Response?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                accountId = null
                token = null
                authorizationMessage = null
                response = null
            }
        }
    }

    fun handleInput(text: String) = text.split(' ').let { message ->
        val command = message.first()
        if (state.token == null && !listOf("authorize", "logout", "end").contains(command)) { //Todo -> enum
            setState {
                authorizationMessage = "Authorization required."
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
                            authorizationMessage = if (response.token != null)
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
                            this.response = response
                        }
                    }
                }
                "withdraw" -> {
                    val amount = message[1]!!.toDouble()
                    scope.launch {
                        val response = Api.withdraw(state.accountId!!, state.token!!, amount)
                        setState {
                            this.response = response
                        }
                    }
                }
                "deposit" -> {
                    val amount = message[1]!!.toDouble()
                    scope.launch {
                        val response = Api.deposit(state.accountId!!, state.token!!, amount)
                        setState {
                            this.response = response
                        }
                    }
                }
                "history" -> {
                    scope.launch {
                        val response = Api.history(state.accountId!!, state.token!!)
                        setState {
                            this.response = response
                        }
                    }
                }
                else -> throw Exception("Unknown command [$message]")
            }
        }
    }

    override fun RBuilder.render() {
        div {
            state.authorizationMessage?.let { p { + "auth message: $it" } }
            state.response?.accountError?.let { p { + "accountError: $it" } }
            state.response?.machineError?.let { p { + "machineError: $it" } }
            state.response?.amount?.let { p { + "amount: $it" } }
            state.response?.balance?.let { p { + "balance: $it" } }
            //state.response?.history.let { p { "auth message: $it" } }
            inputComponent {
                onSubmit = {
                    handleInput(it)
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
