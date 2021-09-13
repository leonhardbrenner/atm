import generated.model.SeedsDto
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
    var registerType: String
    var selected: Int?
    var over: Int?
}

class App : RComponent<RProps, AppState>() {
    //private var tab1Value = Label.Plan.text

    override fun AppState.init() {
        scope.launch {
            setState {
                tabValue = Label.Register.text
                registerType = SeedsDto.DetailedSeed.path
                selected = null
                over = null
            }
        }
    }

    override fun RBuilder.render() {
        //themeContext.Consumer { theme ->
        //    styledDiv {
        //        css { flexGrow = 1.0; backgroundColor = Color(theme.palette.background.paper) }
        //        mAppBar(position = MAppBarPosition.static) {
        //            mTabs(state.tabValue,
        //                onChange = { _, value -> setState { tabValue = value as String } }
        //            ) {
        //                //Todo - app.seeds.register, app.seeds.organize, app.seeds.prioritize
        //                mTab(Label.Register.text, Label.Register.text)
        //            }
        //        }
        //
        //        when (state.tabValue) {
        //            Label.Register.text -> {
        //                register {
        //                    type = state.registerType
        //                    setType = { type ->
        //                        setState { registerType = type}
        //                    }
        //                }
        //            }
        //        }
        //    }
        //}
    }
}

fun RBuilder.app() = child(App::class) {}
