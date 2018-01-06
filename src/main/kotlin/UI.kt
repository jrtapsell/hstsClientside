import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.clear

/**
 * @author James Tapsell
 */
object UI {
    private val loadingScreen = document.getElementById("loading-view") as HTMLDivElement
    private val failScreen = document.getElementById("fail-view") as HTMLDivElement
    private val mainScreen = document.getElementById("main-view") as HTMLDivElement
    private val searchForm = document.getElementById("searchForm") as HTMLFormElement
    private val searchResults = document.getElementById("searchResults") as HTMLDivElement
    private val siteInput = document.getElementById("sitename") as HTMLInputElement

    private fun search() {
        val domain = siteInput.value
        if (domain.isNullOrBlank()) {
            window.alert("Cannot search for null")
            return
        }
        val data = Main.test(domain)
        searchResults.clear()
        for ((browser, browserData) in data) {
            searchResults.div {
                textContent = browser
                addClass("treeDiv")
                for ((platform, platformData) in browserData) {
                    div {
                        textContent = platform
                        addClass("treeDiv")
                        for ((release, releaseData) in platformData) {
                            val css = when (releaseData) {
                                DomainState.NONE -> "state-none"
                                DomainState.JUST_DOMAIN -> "state-single"
                                DomainState.INCLUDE_SUBDOMAINS -> "state-domain"
                            }
                            div {
                                textContent = "${release.name}"
                                val releasePart = release.version.split(".")
                                div {
                                    addClass("version")
                                    releasePart.forEachIndexed { index, partText ->
                                        val last = index == releasePart.size - 1
                                        div {
                                            textContent = partText
                                            addClass("versionChunk")
                                            addClass("versionChunk-$index")
                                            if (!last) {
                                                addClass("versionChunk-middle")
                                            }
                                        }
                                    }
                                }
                                addClass("treeDiv")
                                addClass(css)
                            }
                        }
                    }

                }
            }
        }
    }

    fun run() {
        mainScreen.hidden = true
        failScreen.hidden = true
        searchForm.onsubmit = {
            try {
                search()
            } catch (ex: Exception) {
                console.log(ex)
            }
            false
        }
        Main.init().then {
            console.timeStamp("Loaded")
            loadingScreen.hidden = true
            mainScreen.hidden = false
        }.catch {
            console.timeStamp("Failed")
            loadingScreen.hidden = true
            failScreen.hidden = false
        }
    }

    inline fun HTMLDivElement.div(block: (HTMLDivElement.() -> Unit)) {
        val child = document.createElement("div") as HTMLDivElement
        child.block()
        appendChild(child)
    }

    fun makeDiv(text: String, vararg classes: String): HTMLDivElement {
        val browserDiv = document.createElement("div") as HTMLDivElement
        browserDiv.textContent = text
        browserDiv.className = classes.joinToString(" ")
        return browserDiv
    }
}