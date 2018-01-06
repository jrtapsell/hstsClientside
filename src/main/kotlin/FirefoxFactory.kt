import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser
import kotlin.js.Promise

object FirefoxFactory : ProviderFactory {

    val releases = listOf(
            "mozilla-beta" to "Beta",
            "mozilla-release" to "Release"
    )

    override fun initialise(): Promise<Provider> {
        console.timeStamp("Firefox: Start")
        return Promise{resolve, reject ->
            val releasePromises = releases.map { (releaseBranch, name) ->
                (getReleaseDetails(releaseBranch, name) to getVersionNumber(releaseBranch)).all()
            }
            releasePromises.all().then ({
                val data = it.associate { (data, releaseString) ->
                    Release(data.first, releaseString) to data.second
                }
                console.timeStamp("Firefox: End")
                resolve(Provider("Firefox", mapOf("all" to data)))
            }, reject)
        }
    }

    private fun getReleaseDetails(releaseBranch: String, name: String): Promise<Pair<String, Map<String, Boolean>>> {
        return Promise { resolve, reject ->
                withText("/firefox/releases/$releaseBranch/rss-log") {
                    val tree = DOMParser().parseFromString(it, "text/xml")
                    val link = tree.getElementsByTagName("guid")[0]!!.textContent!!
                    val parts = link.split("/")
                    val hash = parts[parts.size - 1]
                    withText("/firefox/releases/$releaseBranch/raw-file/$hash/security/manager/ssl/nsSTSPreloadList.inc", true) {
                        val lines = it.split("\n")
                            .map { it.trim() }
                        val start = lines.indexOf("%%")
                        val end = lines.lastIndexOf("%%")
                        val usable = lines.subList(start + 1, end)
                        resolve(name to usable.associate {
                            val (domain, subs) = it.split(",")
                            domain to (subs == "1")
                        })
                    }.catch(reject)
                }.catch(reject)
            }

        }

    private fun getVersionNumber(releaseBranch: String): Promise<String> {
        return withText("/firefox/releases/$releaseBranch/raw-file/tip/browser/config/version_display.txt")
    }

}