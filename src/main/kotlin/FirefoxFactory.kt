import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser
import kotlin.js.Promise

object FirefoxFactory : ProviderFactory {
    val releases = listOf(
            "mozilla-beta" to "Beta",
            "mozilla-release" to "Release"
    )
    override fun initialise(): Promise<Provider> {
        console.log("Started loading firefox data")
        return Promise{resolve, reject ->
            val releasePromises = releases.map { (releaseBranch, name) ->
                Promise.all(arrayOf(getReleaseDetails(releaseBranch, name), getVersionNumber(releaseBranch)))
            }.toTypedArray()
            Promise.all(releasePromises).then ({
                val data = it.associate { (data, releaseString) ->
                    data as Pair<String, Map<String, Boolean>>
                    val r = releaseString as String
                    Release(data.first, releaseString) to data.second
                }
                console.log("Loaded firefox data")
                resolve(Provider("Firefox", mapOf("all" to data)))
            }, reject)
        }
    }

    private fun getReleaseDetails(releaseBranch: String, name: String): Promise<Pair<String, Map<String, Boolean>>> {
        return Promise { resolve, reject ->
            val versionHashes = releases.map { (branchName, name) ->
                withText("/firefox/releases/$branchName/rss-log") {
                    val tree = DOMParser().parseFromString(it, "text/xml")
                    val link = tree.getElementsByTagName("guid").get(0)!!.textContent!!
                    val parts = link.split("/")
                    val hash = parts[parts.size - 1]
                    withText("/firefox/releases/$branchName/raw-file/$hash/security/manager/ssl/nsSTSPreloadList.inc", true) {
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
    }

    private fun getVersionNumber(releaseBranch: String): Promise<String> {
        return withText("/firefox/releases/$releaseBranch/raw-file/tip/browser/config/version_display.txt") {
            it
        }
    }

}