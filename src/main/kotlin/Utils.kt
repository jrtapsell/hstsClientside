import org.w3c.fetch.CORS
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import kotlin.browser.window
import kotlin.js.Promise

fun withText(url: String, permaCache: Boolean=false) = withText(url, permaCache){it}
fun <T> withText(url: String, permaCache: Boolean=false, block: (String) -> T): Promise<T> {
    return Promise { resolve, reject ->
        val cache = Headers()
        if (permaCache) {
            cache.append("permaCache", "true")
        }
        val init = RequestInit(
                method = "GET",
                headers = cache,
                body = undefined,
                referrer = undefined,
                referrerPolicy = "",
                mode = RequestMode.CORS,
                credentials = undefined,
                cache = undefined,
                redirect = undefined,
                integrity = "",
                keepalive = undefined,
                window = window
        )
        window.fetch(url, init).then({ promise ->
            promise.text().then({ pageText ->
                resolve(block(pageText))
            }, reject)
        }, reject)
    }
}

fun <T> List<Promise<T>>.all() = Promise.all(this.toTypedArray())
inline fun <reified T,reified U> Pair<Promise<T>, Promise<U>>.all(): Promise<Pair<T, U>> {
    return Promise{resolve, reject ->
        Promise.all(arrayOf(this.first, this.second))
            .then { (first, second) ->
                if(first !is T) throw AssertionError()
                if(second !is U) throw AssertionError()
                resolve(first to second)
            }.catch(reject)
    }
}