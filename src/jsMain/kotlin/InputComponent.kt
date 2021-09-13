import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.form
import react.dom.input

external interface InputProps : RProps {
    var onSubmit: (String) -> Unit
}

external interface InputState : RState {
    var text: String
}

class InputComponent: RComponent<InputProps, InputState>() {
    override fun RBuilder.render() {

        val submitHandler: (Event) -> Unit = {
            it.preventDefault()
            props.onSubmit(state.text)
            setState {
                text = ""
            }
        }

        val changeHandler: (Event) -> Unit = {
            val value = (it.target as HTMLInputElement).value
            setState {
                text = value
            }
        }

        form {
            attrs.onSubmitFunction = submitHandler
            input(InputType.text) {
                attrs.onChangeFunction = changeHandler
                attrs.value = state.text
            }
        }
    }
}

fun RBuilder.inputComponent(handler: InputProps.() -> Unit) = child(InputComponent::class) { attrs { handler() } }
