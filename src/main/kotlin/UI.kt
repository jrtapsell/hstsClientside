import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.js.Promise

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
            val browserDiv = makeDiv(browser, "treeDiv")
            searchResults.appendChild(browserDiv)
            for ((platform, platformData) in browserData) {
                val platformDiv = makeDiv(platform, "treeDiv")
                browserDiv.appendChild(platformDiv)
                for ((release, releaseData) in platformData) {
                    val css = when (releaseData) {
                        DomainState.NONE -> "state-none"
                        DomainState.JUST_DOMAIN -> "state-single"
                        DomainState.INCLUDE_SUBDOMAINS -> "state-domain"
                    }
                    val releaseDiv = makeDiv("${release.name} [${release.version}]", "treeDiv", css)
                    platformDiv.appendChild(releaseDiv)
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

    fun makeDiv(text: String, vararg classes: String): HTMLDivElement {
        val browserDiv = document.createElement("div") as HTMLDivElement
        browserDiv.textContent = text
        browserDiv.className = classes.joinToString(" ")
        return browserDiv
    }
}