import kotlin.js.Promise

object Main {

    fun init(): Promise<Unit> {
        val promises = factories.map { it.initialise() }.toTypedArray()
        return Promise.all(promises).then {
            providers = it.toList()
        }
    }

    private val factories: List<ProviderFactory> = listOf(ChromeFactory, FirefoxFactory)

    private lateinit var providers: List<Provider>

    fun test(url: String) = providers.associate {
        it.name to it.checkDomain(url)
    }
}