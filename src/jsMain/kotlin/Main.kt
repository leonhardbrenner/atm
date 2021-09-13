import com.ccfraser.muirwik.components.mThemeProvider
import com.ccfraser.muirwik.components.styles.mStylesProvider
import react.dom.render

import kotlinx.browser.document

//TODO - https://github.com/gaearon/react-hot-loader
@JsNonModule
@JsModule("react-hot-loader")
private external val hotModule: dynamic
private val hot = hotModule.hot
private val module = js("module")

fun main() {
    render(document.getElementById("root")) {
        mStylesProvider("jss-insertion-point") {
            mThemeProvider {
                val hotWrapper = hot(module)
                hotWrapper(app())
            }
        }
    }
}