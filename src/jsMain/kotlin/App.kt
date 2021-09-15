import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*

private val scope = MainScope()

enum class Label(val text: String) {
    Register("Register")
}

//TODO - move this back into Plan2. Then we can lift state and compose new components.
external interface AppState : RState {
    var tabValue: String
    var selected: Int?
    var over: Int?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                tabValue = Label.Register.text
                selected = null
                over = null
            }
        }
    }

    override fun RBuilder.render() {
    }
}

fun RBuilder.app() = child(App::class) {}
