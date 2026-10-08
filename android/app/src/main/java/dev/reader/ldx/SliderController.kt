package dev.reader.ldx

import android.util.Log
import androidx.compose.runtime.*
import androidx.room.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.readium.r2.shared.publication.Locator

@Entity(tableName = "slider_return")
data class SliderReturnRecord(@PrimaryKey val bookId: String, val locatorJson: String)
@Dao interface SliderReturnDao {
    @Query("SELECT * FROM slider_return WHERE bookId = :bookId") suspend fun get(bookId: String): SliderReturnRecord?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(record: SliderReturnRecord)
}

/** One return target per book. Only explicit slider commitment or Return changes it. */
class SliderController(private val storage: ReaderStorage, private val engine: ReadingEngine, private val scope: CoroutineScope) {
    var positions by mutableStateOf<List<Locator>>(emptyList()); private set
    var value by mutableFloatStateOf(0f); private set
    var previewing by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var notice by mutableStateOf(""); private set
    var returnTarget by mutableStateOf<Locator?>(null); private set
    private var source: Locator? = null
    private var movement: Job? = null
    private var previewReady = false
    private var lastPreviewAt = 0L
    suspend fun opened() {
        movement?.cancel(); previewing = false; source = null; previewReady = false
        positions = engine.sliderPositions()
        returnTarget = storage.database.slider().get(engine.book!!.id)?.let { Locator.fromJSON(JSONObject(it.locatorJson)) }
        value = fraction(engine.committed.value)
        Log.i("ReaderEvidence", "SLIDER_STATE return=${returnTarget?.toJSON()} positions=${positions.size}")
    }
    fun fraction(locator: Locator?): Float = engine.sliderFraction(locator)
    fun preview(fraction: Float) {
        if (busy || positions.isEmpty() || engine.bookNote.value != null) return
        if (!previewing) { source = engine.committed.value ?: return; previewing = true; Log.i("ReaderEvidence", "SLIDER_BEGIN source=${source?.toJSON()}") }
        value = fraction
        val target = positions[(fraction * positions.size).toInt().coerceIn(positions.indices)]
        previewReady = false
        // Suppress commitment immediately, before any navigator movement or callbacks.
        engine.transient.value = true
        movement?.cancel()
        movement = scope.launch {
            try {
                // Throttle against the last issued movement, so continuous pointer updates
                // still move the live page rather than waiting for a quiet drag.
                delay((100 - (android.os.SystemClock.elapsedRealtime() - lastPreviewAt)).coerceAtLeast(0))
                lastPreviewAt = android.os.SystemClock.elapsedRealtime()
                withTimeout(5000) { while (!engine.preview(target)) delay(100) }
                engine.awaitPreviewDestination(target); previewReady = true
                Log.i("ReaderEvidence", "SLIDER_PREVIEW requested=${target.toJSON()} visible=${engine.visible.value?.toJSON()}")
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                notice = "Preview failed: ${e.message}"; Log.e("ReaderEvidence", notice, e)
            }
        }
    }
    fun commitFromPage(): Boolean {
        if (!previewing) return false
        if (!busy) scope.launch { try { commit() } catch (e: Exception) { notice = "Slider commit failed: ${e.message}"; Log.e("ReaderEvidence", notice, e) } }
        return true
    }
    private suspend fun commit(returnAnchor: Locator? = null) {
        val prior = source ?: return
        busy = true
        try {
            movement?.join()
            check(previewReady) { "Preview has not reached its destination" }
            val result = engine.commitPreview(returnAnchor) { chosen ->
                storage.database.withTransaction {
                    storage.books.commit(engine.book!!.id, chosen.toJSON().toString())
                    storage.database.slider().save(SliderReturnRecord(engine.book!!.id, prior.toJSON().toString()))
                }
            } ?: return
            returnTarget = prior; previewing = false; source = null; previewReady = false
            Log.i("ReaderEvidence", "SLIDER_COMMIT source=${prior.toJSON()} destination=${result.toJSON()} return=${returnTarget?.toJSON()}")
        } finally { busy = false }
    }
    suspend fun cancel() {
        if (busy) return
        movement?.cancelAndJoin()
        engine.cancelPreview()
        previewing = false; source = null; previewReady = false
        value = fraction(engine.committed.value)
        Log.i("ReaderEvidence", "SLIDER_CANCEL committed=${engine.committed.value?.toJSON()} return=${returnTarget?.toJSON()}")
    }
    suspend fun toggleReturn() {
        if (busy || previewing) return
        val target = returnTarget ?: return
        source = engine.committed.value ?: return
        previewing = true; previewReady = false; busy = true
        try {
            withTimeout(5000) { while (!engine.preview(target)) delay(100) }
            // A locator with a selector can be checked against the rendered page.
            engine.awaitPreviewDestination(target)
            previewReady = true
        } finally { busy = false }
        commit(target)
    }
}
