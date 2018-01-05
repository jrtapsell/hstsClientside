import kotlin.js.Promise

enum class DomainState{INCLUDE_SUBDOMAINS, JUST_DOMAIN, NONE}

typealias DS = Map<String, Map<Release, Map<String, Boolean>>>

data class Release(val name: String, val version: String)
class Provider(val name: String, val dataStore: DS) {
    fun checkDomain(name: String): Map<String, Map<Release, DomainState>> {
        val ret = mutableMapOf<String, MutableMap<Release, DomainState>>()
        dataStore.forEach { (os, osPayload) ->
            osPayload.forEach { (branch, branchPayload) ->
                val value = branchPayload[name]
                val state = if (value != null) {
                    if (value) {
                        DomainState.INCLUDE_SUBDOMAINS
                    } else {
                        DomainState.JUST_DOMAIN
                    }
                } else {
                    DomainState.NONE
                }
                ret.getOrPut(os, { mutableMapOf()})
                    .put(branch, state)
            }
        }
        return ret
    }
}

interface ProviderFactory {
    fun initialise(): Promise<Provider>
}