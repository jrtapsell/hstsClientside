import kotlin.browser.window


@Suppress("unused")
fun start() {
    window.navigator.serviceWorker.register("sw.js")
    UI.run()
}