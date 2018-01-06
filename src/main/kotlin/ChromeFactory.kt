import kotlin.js.Promise

typealias VersionsResponse = Array<OSResponse>
data class OSResponse(val os: String, val versions: Array<Version>)
data class Version(val chromium_commit: String, val channel: String, val version: String)

data class JSONData(val entries: Array<Site>)
data class Site(val name: String, val include_subdomains: Boolean)


object ChromeFactory: ProviderFactory {
    data class Version(val os:String, val branch: String, val commit: String, val versionNumber: Release)
    override fun initialise(): Promise<Provider> {
        console.timeStamp("Chrome: Start")
        return Promise{resolve, reject ->
            withText("/chrome-versions") {
                console.timeStamp("Chrome: Versions found")
                val versions = JSON.parse<VersionsResponse>(it)

                val all = versions.flatMap { os -> os.versions.map { os to it } }
                    .map { Version(it.first.os, it.second.channel, it.second.chromium_commit, Release(it.second.channel, it.second.version)) }
                    .filter { it.commit != undefined }

                val liveHashes = all.map { it.commit }.distinct()

                val promises = liveHashes.map { hash ->
                    val url = "/chromium/chromium/src/+/$hash/net/http/transport_security_state_static.json?format=TEXT"
                    withText(url, true) {
                        val raw = atob(it)
                        val lines = raw.split("\n")
                            .map { it.trim() }
                            .filter { !it.startsWith("//") }
                            .joinToString("\n")
                        val data = JSON.parse<JSONData>(lines)
                        hash to data.entries.associate { it.name to it.include_subdomains }
                    }
                }

                promises.all().then ({
                    val dataStore = mutableMapOf<
                            String,
                            MutableMap<
                                    Release,
                                    Map<String, Boolean>>>()
                    val allData = it.associate { it }
                    all.forEach { (os, _, commit, release) ->
                        dataStore.getOrPut(os, { mutableMapOf() })
                            .put(release, allData[commit]?.toMutableMap() ?: mutableMapOf())
                    }
                    console.timeStamp("Chrome: End")
                    resolve(Provider("Chrome", dataStore))
                }, reject)
            }.catch(reject)
        }

    }
}