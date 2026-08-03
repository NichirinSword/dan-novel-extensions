package eu.kanade.tachiyomi.network

import okhttp3.Headers
import okhttp3.Request

fun GET(url: String, headers: Headers = Headers.Builder().build()): Request =
    Request.Builder().url(url).headers(headers).build()
