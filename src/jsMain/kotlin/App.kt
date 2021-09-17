import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*

private val scope = MainScope()

external interface AppState : RState {
    var selected: Int?
    var over: Int?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        scope.launch {
            setState {
                selected = null
                over = null
            }
        }
    }

    override fun RBuilder.render() {
        + "hello"
    }
}

fun RBuilder.app() = child(App::class) {}
