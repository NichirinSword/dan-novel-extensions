package eu.kanade.tachiyomi.source.model

import java.net.URI

/**
 * Real Tachiyomi convenience helpers: sources store `url` as just the
 * path (no scheme/host), so lookups later can be built against whatever
 * baseUrl the source declares. RoyalRoad.kt uses these directly.
 */
fun SManga.setUrlWithoutDomain(url: String) {
    this.url = getUrlWithoutDomain(url)
}

fun SChapter.setUrlWithoutDomain(url: String) {
    this.url = getUrlWithoutDomain(url)
}

private fun getUrlWithoutDomain(orig: String): String {
    return try {
        val uri = URI(orig)
        var out = uri.path.orEmpty()
        if (uri.query != null) out += "?" + uri.query
        if (uri.fragment != null) out += "#" + uri.fragment
        out
    } catch (e: Exception) {
        orig
    }
}
