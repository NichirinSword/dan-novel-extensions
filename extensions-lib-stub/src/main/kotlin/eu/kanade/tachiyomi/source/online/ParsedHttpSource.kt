package eu.kanade.tachiyomi.source.online

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    private fun Response.asJsoup(): Document = Jsoup.parse(body?.string().orEmpty(), request.url.toString())

    // ---- Popular ----
    abstract fun popularMangaSelector(): String
    abstract fun popularMangaFromElement(element: Element): SManga
    open fun popularMangaNextPageSelector(): String? = null

    override fun popularMangaParse(response: Response): List<SManga> {
        val doc = response.asJsoup()
        return doc.select(popularMangaSelector()).map { popularMangaFromElement(it) }
    }

    // ---- Latest ----
    abstract fun latestUpdatesSelector(): String
    abstract fun latestUpdatesFromElement(element: Element): SManga
    open fun latestUpdatesNextPageSelector(): String? = null

    override fun latestUpdatesParse(response: Response): List<SManga> {
        val doc = response.asJsoup()
        return doc.select(latestUpdatesSelector()).map { latestUpdatesFromElement(it) }
    }

    // ---- Search ----
    abstract fun searchMangaSelector(): String
    abstract fun searchMangaFromElement(element: Element): SManga
    open fun searchMangaNextPageSelector(): String? = null

    override fun searchMangaParse(response: Response): List<SManga> {
        val doc = response.asJsoup()
        return doc.select(searchMangaSelector()).map { searchMangaFromElement(it) }
    }

    // ---- Details ----
    abstract fun mangaDetailsParse(document: Document): SManga
    override fun mangaDetailsParse(response: Response): SManga = mangaDetailsParse(response.asJsoup())

    // ---- Chapters ----
    abstract fun chapterListSelector(): String
    abstract fun chapterFromElement(element: Element): SChapter

    override fun chapterListParse(response: Response): List<SChapter> {
        val doc = response.asJsoup()
        return doc.select(chapterListSelector()).map { chapterFromElement(it) }
    }

    // ---- Pages ----
    abstract fun pageListParse(document: Document): List<Page>
    override fun pageListParse(response: Response): List<Page> = pageListParse(response.asJsoup())

    abstract fun imageUrlParse(document: Document): String
    override fun imageUrlParse(response: Response): String = imageUrlParse(response.asJsoup())
}
