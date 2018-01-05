import kotlin.browser.window


@Suppress("unused")
fun start() {
    console.timeStamp("Started")
    window.navigator.serviceWorker.register("sw.js")
    UI.run()
}