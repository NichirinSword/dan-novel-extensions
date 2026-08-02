package eu.kanade.tachiyomi.extension.en.royalroad

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RoyalRoad source for Mihon/Aniyomi-based readers (Dantotsu novel extensions).
 *
 * NOTE ON DANTOTSU COMPATIBILITY:
 * Dantotsu confirmed uses the real Mihon extension-lib (`eu.kanade.tachiyomi.*`
 * package, `HttpSource`/`ParsedHttpSource` classes) to load novel sources -
 * this is the same base class manga extensions use. Chapters here are
 * treated the way manga chapters are (SChapter), and the chapter "pages"
 * (getPageList/pageListParse) return the chapter text instead of images.
 *
 * The one piece I cannot 100% confirm without Dantotsu's private
 * AniyomiAdapter.kt source: exactly HOW it expects text to be packed into
 * the Page objects it reads back out. I've implemented the most common
 * convention used by novel-as-manga hacks (Page.url holds a marker,
 * Page.imageUrl holds the raw chapter HTML/text). If Dantotsu expects a
 * different field, this is the one method (`pageListParse`) you'll need to
 * adjust - everything else (search, details, chapter list) follows the
 * standard, well-documented Mihon HttpSource contract and should compile
 * and run as-is.
 */
class RoyalRoad : ParsedHttpSource() {

    override val name = "RoyalRoad"
    override val baseUrl = "https://www.royalroad.com"
    override val lang = "en"
    override val supportsLatest = true

    // ------------------------------------------------------------------
    // Popular (Best Rated list)
    // ------------------------------------------------------------------

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/fictions/best-rated?page=$page", headers)

    override fun popularMangaSelector() = "div.fiction-list-item"

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        val titleLink = element.selectFirst("h2.fiction-title a")
        title = titleLink?.text().orEmpty()
        setUrlWithoutDomain(titleLink?.attr("href").orEmpty())
        thumbnail_url = element.selectFirst("img")?.attr("abs:src")
    }

    override fun popularMangaNextPageSelector() = "ul.pagination li.page-item:not(.disabled) a[rel=next]"

    // ------------------------------------------------------------------
    // Latest (Latest Updates list)
    // ------------------------------------------------------------------

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/fictions/latest-updates?page=$page", headers)

    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/fictions/search?title=${query.trim()}&page=$page", headers)

    override fun searchMangaSelector() = "div.search-content div.fiction-list-item, div.fiction-list-item"
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // ------------------------------------------------------------------
    // Fiction (manga) details
    // ------------------------------------------------------------------

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1.font-white, div.fic-title h1")?.text().orEmpty()
        author = document.selectFirst("h4.font-white a, span[property=author] a")?.text()
        description = document.selectFirst("div.description, div.hidden-content")?.text()
        thumbnail_url = document.selectFirst("div.fic-header img, img.thumbnail")?.attr("abs:src")
        genre = document.select("span.tags a.fiction-tag").joinToString { it.text() }
        val statusText = document.select("span.label").firstOrNull {
            it.text().contains("ONGOING", true) ||
                it.text().contains("COMPLETED", true) ||
                it.text().contains("HIATUS", true) ||
                it.text().contains("STUB", true) ||
                it.text().contains("DROPPED", true)
        }?.text().orEmpty()
        status = when {
            statusText.contains("COMPLETED", true) -> SManga.COMPLETED
            statusText.contains("ONGOING", true) -> SManga.ONGOING
            statusText.contains("HIATUS", true) -> SManga.ON_HIATUS
            statusText.contains("DROPPED", true) -> SManga.CANCELLED
            else -> SManga.UNKNOWN
        }
    }

    // ------------------------------------------------------------------
    // Chapter list
    // ------------------------------------------------------------------

    override fun chapterListSelector() = "table#chapters tbody tr[data-url]"

    private val chapterDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        val link = element.attr("data-url")
        setUrlWithoutDomain(link)
        name = element.selectFirst("td a")?.text()?.trim().orEmpty()
        val dateText = element.selectFirst("td time")?.attr("title")
        date_upload = runCatching { chapterDateFormat.parse(dateText.orEmpty())?.time }
            .getOrNull() ?: 0L
    }

    // Chapters on RoyalRoad are listed oldest-first already; Mihon expects
    // newest-first in the UI, so reverse.
    override fun chapterListParse(response: Response): List<SChapter> =
        super.chapterListParse(response).reversed()

    // ------------------------------------------------------------------
    // Chapter text ("pages")
    // ------------------------------------------------------------------

    override fun pageListParse(document: Document): List<Page> {
        val content = document.selectFirst("div.chapter-content")
            ?: document.selectFirst("div.chapter-inner.chapter-content")
        val text = content?.let { cleanChapterText(it) }.orEmpty()

        // Convention used by several novel-in-manga-reader adapters:
        // a single Page whose "imageUrl" carries the extracted chapter text,
        // with url left as the source page. Adjust here if Dantotsu's
        // AniyomiAdapter expects a different field/marker.
        return listOf(Page(0, document.location(), text))
    }

    override fun imageUrlParse(document: Document): String = ""

    /**
     * Strips RoyalRoad's anti-scraping "styled to look invisible" spam
     * paragraphs (elements with display:none / opacity:0 / zero font-size
     * inserted between real paragraphs) before returning clean chapter text.
     */
    private fun cleanChapterText(content: Element): String {
        content.select("p, div").forEach { el ->
            val style = el.attr("style")
            if (style.contains("display:none") ||
                style.contains("display: none") ||
                style.contains("opacity:0") ||
                style.contains("font-size:0")
            ) {
                el.remove()
            }
        }
        return content.select("p").joinToString("\n\n") { it.text() }
            .ifBlank { content.text() }
    }

    // ------------------------------------------------------------------
    // Not used for this source
    // ------------------------------------------------------------------

    override fun getFilterList(): FilterList = FilterList()
}
