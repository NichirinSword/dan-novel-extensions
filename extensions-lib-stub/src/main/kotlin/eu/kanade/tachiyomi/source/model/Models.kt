package eu.kanade.tachiyomi.source.model

interface SManga {
    var url: String
    var title: String
    var artist: String?
    var author: String?
    var description: String?
    var genre: String?
    var status: Int
    var thumbnail_url: String?
    var initialized: Boolean

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        fun create(): SManga = SMangaImpl()
    }
}

class SMangaImpl : SManga {
    override var url: String = ""
    override var title: String = ""
    override var artist: String? = null
    override var author: String? = null
    override var description: String? = null
    override var genre: String? = null
    override var status: Int = SManga.UNKNOWN
    override var thumbnail_url: String? = null
    override var initialized: Boolean = false
}

interface SChapter {
    var url: String
    var name: String
    var date_upload: Long
    var chapter_number: Float
    var scanlator: String?

    companion object {
        fun create(): SChapter = SChapterImpl()
    }
}

class SChapterImpl : SChapter {
    override var url: String = ""
    override var name: String = ""
    override var date_upload: Long = 0
    override var chapter_number: Float = -1f
    override var scanlator: String? = null
}

data class Page(
    val index: Int,
    val url: String = "",
    var imageUrl: String? = null,
) {
    var status: Int = 0
}

abstract class Filter<T>(val name: String, var state: T) {
    open class Header(name: String) : Filter<Boolean>(name, false)
    open class TriState(name: String) : Filter<Int>(name, 0)
    open class Text(name: String) : Filter<String>(name, "")
    open class CheckBox(name: String, state: Boolean = false) : Filter<Boolean>(name, state)
    open class Select<V>(name: String, val values: Array<V>, state: Int = 0) : Filter<Int>(name, state)
    open class Group<V>(name: String, initialState: List<V>) : Filter<List<V>>(name, initialState)
}

open class FilterList(list: List<Filter<*>> = emptyList()) : ArrayList<Filter<*>>(list)
