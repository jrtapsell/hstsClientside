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
                Promise.all(arrayOf(withText("https://agitated-colden-c5fdf6.netlify.com/firefox/releases/$releaseBranch/raw-file/tip/security/manager/ssl/nsSTSPreloadList.inc") {
                    val lines = it.split("\n")
                        .map { it.trim() }
                    val start = lines.indexOf("%%")
                    val end = lines.lastIndexOf("%%")
                    val usable = lines.subList(start + 1, end)
                    name to usable.associate {
                        val (domain, subs) = it.split(",")
                        domain to (subs == "1")
                    }
                },withText("https://agitated-colden-c5fdf6.netlify.com/firefox/releases/$releaseBranch/raw-file/tip/browser/config/version_display.txt") {
                    it
                }))
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

}