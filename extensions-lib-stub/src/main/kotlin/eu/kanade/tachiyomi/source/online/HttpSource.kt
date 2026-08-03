package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class HttpSource {
    abstract val name: String
    abstract val baseUrl: String
    abstract val lang: String
    abstract val supportsLatest: Boolean

    open val client: OkHttpClient = OkHttpClient()
    open val headers: Headers = Headers.Builder().build()

    open fun popularMangaRequest(page: Int): Request = throw NotImplementedError()
    abstract fun popularMangaParse(response: Response): List<SManga>

    open fun latestUpdatesRequest(page: Int): Request = throw NotImplementedError()
    abstract fun latestUpdatesParse(response: Response): List<SManga>

    open fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        throw NotImplementedError()
    abstract fun searchMangaParse(response: Response): List<SManga>

    open fun mangaDetailsRequest(manga: SManga): Request = GET(baseUrl + manga.url)
    abstract fun mangaDetailsParse(response: Response): SManga

    open fun chapterListRequest(manga: SManga): Request = GET(baseUrl + manga.url)
    abstract fun chapterListParse(response: Response): List<SChapter>

    open fun pageListRequest(chapter: SChapter): Request = GET(baseUrl + chapter.url)
    abstract fun pageListParse(response: Response): List<Page>

    abstract fun imageUrlParse(response: Response): String

    open fun getFilterList(): FilterList = FilterList()

    private fun GET(url: String) = Request.Builder().url(url).headers(headers).build()
}
