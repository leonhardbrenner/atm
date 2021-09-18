import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*
import react.dom.div

private val scope = MainScope()

const val initialDisplay = "What would you like to do?"
external interface AppState : RState {
    var display: String?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                display = initialDisplay
            }
        }
    }

    fun handleInput(text: String) {
        setState {
            display = text
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
