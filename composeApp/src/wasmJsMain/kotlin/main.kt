import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.ExperimentalComposeUiApi
import dev.prooflens.app.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
