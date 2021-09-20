import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.AccountId
import model.Response
import model.Token
import react.*
import react.dom.*

private val scope = MainScope()

external interface AppState : RState {
    var running: Boolean
    var accountId: AccountId?
    var token: Token?
    var response: Response?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                running = true
                accountId = null
                token = null
                response = null
            }
        }
    }

    fun handleInput(text: String) = text.split(' ').let { message ->
        val command = message.first()
        if (state.token == null && !listOf("authorize", "logout", "end").contains(command)) { //Todo -> enum
            setState {
                response = Response(authorizationError = "Authorization required.")
            }
        } else {
            when (command) {
                "authorize" -> {
                    scope.launch {
                        Api.authorize(accountId = message[1], pin = message[2]).let {
                            setState {
                                accountId = message[1]
                                token = it.token
                            }
                            setState {
                                response = it.copy(
                                    authorizationError = if (it.token != null)
                                        "${state.accountId} successfully authorized."
                                    else
                                        "Authorization failed."
                                )
                            }
                        }
                    }
                }
                "logout" -> {
                    scope.launch {
                        Api.logout(state.accountId!!, state.token!!)
                        setState {
                            accountId = null
                            token = null
                            response = null
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
                "end" -> {
                    setState {
                        this.running = false
                    }
                }
                else -> throw Exception("Unknown command [$message]")
            }
        }
    }
    fun Double.round(decimals: Int) = asDynamic().toFixed(decimals)
    override fun RBuilder.render() {
        if (!state.running)
            return
        div {
            state.response?.authorizationError?.let { p { + it } }
            state.response?.accountError?.let { p { + it } }
            state.response?.machineError?.let { p { + it } }
            state.response?.amount?.let { p { + "Amount Dispensed: ${it.round(2)}" } } //Todo - withdrawAmount and depositAmount
            state.response?.balance?.let { p { + "Current balance: ${it.round(2)}" } }
            state.response?.history?.let {
                if (it.isEmpty())
                    +"No history found"
                else
                    table {
                        it.forEach {
                            tr {
                                td { +it.formatedDatetime }
                                td { +it.amount.round(2).toString() }
                                td { +it.balance.round(2).toString() }
                            }
                        }
                    }
            }
            inputComponent {
                onSubmit = {
                    handleInput(it)
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
