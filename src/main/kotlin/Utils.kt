import org.w3c.fetch.CORS
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import kotlin.browser.window
import kotlin.js.Promise

/**
 * @author James Tapsell
 */
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
        //console.log("Request", url, "($actualURL)")
        window.fetch(url, init).then({ promise ->
            promise.text().then({ pageText ->
                //console.log("Response", pageText.length)
                resolve(block(pageText))
            }, reject)
        }, reject)
    }
}