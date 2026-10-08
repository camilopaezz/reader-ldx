package dev.reader.ldx

import android.util.Log
import androidx.compose.runtime.*
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.Selection
import org.readium.r2.shared.publication.Locator
import java.util.UUID

/** Save completes in Room before the editor closes or success becomes visible. */
class AnnotationController(private val store: AnnotationStore, private val engine: ReadingEngine) {
    var records by mutableStateOf<List<AnnotationRecord>>(emptyList()); private set
    var showList by mutableStateOf(false)
    var editing by mutableStateOf<AnnotationRecord?>(null)
    var busy by mutableStateOf(false); private set
    var notice by mutableStateOf(""); private set
    init {
        engine.onNavigatorReady = { reload() }
        engine.onAnnotationActivated = { id -> records.find { it.id == id }?.let { editing = it } }
    }
    suspend fun reload() {
        val id = engine.book?.id ?: return
        records = store.records.all(id)
        val decorations = records.filter { it.kind == "passage" }.map {
            Decoration(it.id, it.locator(), Decoration.Style.Highlight(colorValue(it.color)))
        }
        engine.applyAnnotationDecorations(decorations)
        kotlinx.coroutines.delay(150)
        engine.logAnnotationRanges()
        records.forEach { Log.i("ReaderEvidence", "ANNOTATION_RESTORED id=${it.id} kind=${it.kind} color=${it.color} note=${it.note} range=${it.locatorJson}") }
    }
    fun begin(selection: Selection, note: Boolean) {
        val id = engine.book?.id ?: return
        val locator = selection.locator
        editing = records.firstOrNull { it.kind == "passage" && it.locatorJson == locator.toJSON().toString() }
            ?: AnnotationRecord(UUID.randomUUID().toString(), id, "passage", locator.toJSON().toString(), chapter(locator), locator.text.highlight.orEmpty())
        notice = if (note) "Write an annotation note, then Save." else "Choose a highlight color, then Save."
    }
    suspend fun save(record: AnnotationRecord) {
        busy = true
        try {
            store.records.save(record)
            Log.i("ReaderEvidence", "ANNOTATION_SAVED id=${record.id} kind=${record.kind} color=${record.color} note=${record.note} range=${record.locatorJson}")
            // The durable write is already finished. Reapplying decorations is presentation only.
            reload()
            editing = null
            engine.clearSelection()
            notice = if (record.kind == "bookmark") "Bookmark saved" else "Annotation saved"
        } finally { busy = false }
    }
    suspend fun remove(record: AnnotationRecord) {
        busy = true
        try {
            store.records.remove(record.id)
            Log.i("ReaderEvidence", "ANNOTATION_REMOVED id=${record.id}")
            reload()
            editing = null
            notice = "Annotation removed"
        } finally { busy = false }
    }
    suspend fun bookmark() {
        val id = engine.book?.id ?: return
        val locator = engine.committed.value ?: engine.visible.value ?: return
        save(AnnotationRecord(UUID.randomUUID().toString(), id, "bookmark", locator.toJSON().toString(), chapter(locator), locator.text.highlight.orEmpty().ifBlank { locator.title.orEmpty() }))
    }
    fun navigate(record: AnnotationRecord) {
        showList = false
        editing = null
        engine.clearSelection()
        engine.navigateCommitted(record.locator())
        Log.i("ReaderEvidence", "ANNOTATION_NAVIGATE kind=${record.kind} range=${record.locatorJson}")
    }
    fun dismiss(): Boolean {
        if (busy) return true
        if (editing != null) { editing = null; return true }
        if (showList) { showList = false; return true }
        return false
    }
    private fun chapter(locator: Locator): String {
        val href = locator.href.toString().substringBefore('#')
        return engine.publication.tableOfContents.firstOrNull { it.href.toString().substringBefore('#') == href }?.title
            ?: locator.title ?: href.substringAfterLast('/')
    }
    companion object {
        val colors = listOf("Yellow", "Aqua", "Pink", "Green")
        fun colorValue(name: String): Int = when (name) {
            "Aqua" -> 0xFF80DEEA.toInt(); "Pink" -> 0xFFF48FB1.toInt()
            "Green" -> 0xFFA5D6A7.toInt(); else -> 0xFFFFE082.toInt()
        }
    }
}
