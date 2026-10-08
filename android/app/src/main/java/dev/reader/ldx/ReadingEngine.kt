@file:OptIn(org.readium.r2.shared.ExperimentalReadiumApi::class)
package dev.reader.ldx

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import org.readium.r2.navigator.Selection
import org.readium.r2.navigator.epub.*
import org.readium.r2.navigator.input.*
import org.readium.r2.navigator.util.DirectionalNavigationAdapter
import org.readium.r2.shared.publication.*
import org.readium.r2.shared.publication.services.isRestricted
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File

/** App-owned commitment is independent of Readium's current visible page. */
class ReadingEngine(private val activity: FragmentActivity, private val storage: ReaderStorage) {
    private val scope get() = activity.lifecycleScope
    lateinit var navigator: EpubNavigatorFragment; private set
    lateinit var publication: Publication; private set
    var book: BookRecord? = null; private set
    val visible = MutableStateFlow<Locator?>(null)
    val committed = MutableStateFlow<Locator?>(null)
    val selection = MutableStateFlow<Selection?>(null)
    val transient = MutableStateFlow(false)
    val bookNote = MutableStateFlow<BookNote?>(null)
    private var observer: Job? = null
    private var reflowing = false
    private var preserveAnchor = false
    private var containerId = 0
    private var generation = 0
    private val persistence = kotlinx.coroutines.sync.Mutex()
    var onCenterTap: () -> Unit = {}
    var actionModeCallback: android.view.ActionMode.Callback? = null

    suspend fun open(record: BookRecord, container: Int, font: Double, margins: Double) {
        containerId = container
        generation++
        val gen = generation
        observer?.cancel()
        if (::navigator.isInitialized) activity.supportFragmentManager.beginTransaction().remove(navigator).commitNow()
        if (::publication.isInitialized) publication.close()
        val http = DefaultHttpClient()
        val assets = AssetRetriever(activity.contentResolver, http)
        val asset = assets.retrieve(File(record.path).toUrl()).getOrElse { error("EPUB import failed: $it") }
        publication = PublicationOpener(DefaultPublicationParser(activity, httpClient = http, assetRetriever = assets, pdfFactory = null)).open(asset, allowUserInteraction = false).getOrElse { error("Cannot open EPUB: $it") }
        require(!publication.isRestricted) { "Encrypted publications are outside this prototype" }
        book = record
        val locator = record.committedLocator?.let { Locator.fromJSON(JSONObject(it)) }
        preserveAnchor = locator != null
        committed.value = locator
        selection.value = null
        transient.value = false
        bookNote.value = null
        val factory = EpubNavigatorFactory(publication).createFragmentFactory(
            initialLocator = locator,
            initialPreferences = preferences(font, margins),
            listener = object : EpubNavigatorFragment.Listener {
                override fun onExternalLinkActivated(url: org.readium.r2.shared.util.AbsoluteUrl) { Log.i("ReaderEvidence", "EXTERNAL_LINK unsupported=$url") }
                override fun shouldFollowInternalLink(link: Link, context: org.readium.r2.navigator.HyperlinkNavigator.LinkContext?): Boolean {
                    if (context is org.readium.r2.navigator.HyperlinkNavigator.FootnoteContext) {
                        bookNote.value = BookNote(link.href.toString(), context.noteContent, committed.value)
                        Log.i("ReaderEvidence", "BOOK_NOTE_OPEN target=${link.href} source=${committed.value?.toJSON()}")
                        return false
                    }
                    if (!transient.value && !reflowing) preserveAnchor = false
                    Log.i("ReaderEvidence", "ORDINARY_LINK target=${link.href}")
                    return true
                }
            },
            configuration = EpubNavigatorFragment.Configuration { selectionActionModeCallback = actionModeCallback }
        )
        navigator = factory.instantiate(activity.classLoader, EpubNavigatorFragment::class.java.name) as EpubNavigatorFragment
        activity.supportFragmentManager.beginTransaction().replace(container, navigator).commitNow()
        val directional = DirectionalNavigationAdapter(navigator)
        navigator.addInputListener(object : InputListener {
            override fun onTap(event: TapEvent): Boolean {
                if (reflowing) return true
                val edge = navigator.publicationView.width * 0.3
                if (event.point.x < edge || event.point.x > navigator.publicationView.width - edge) {
                    if (!transient.value) preserveAnchor = false
                }
                return directional.onTap(event)
            }
            override fun onDrag(event: DragEvent): Boolean {
                if (reflowing) return true
                if (kotlin.math.abs(event.offset.x) > 50 && !transient.value) preserveAnchor = false
                return false
            }
        })
        navigator.addInputListener(object : InputListener {
            override fun onTap(event: TapEvent): Boolean { onCenterTap(); return true }
        })
        storage.opened(record.id)
        observer = scope.launch {
            // Readium notifies settled page locations; obtain its stable first visible HTML block.
            navigator.currentLocator.collectLatest { current ->
                delay(250)
                if (gen != generation) return@collectLatest
                val anchor = navigator.firstVisibleElementLocator()
                val located = current.copy(
                    locations = current.locations.copy(otherLocations = current.locations.otherLocations + (anchor?.locations?.otherLocations ?: emptyMap())),
                    text = anchor?.text ?: current.text
                )
                visible.value = located
                Log.i("ReaderEvidence", "VISIBLE ${located.toJSON()}")
                if (bookNote.value == null && !transient.value && !reflowing && !preserveAnchor) saveCommit(located)
            }
        }
    }
    private fun preferences(font: Double, margins: Double) = EpubPreferences(fontSize = font / 100.0, pageMargins = margins, fontFamily = org.readium.r2.navigator.preferences.FontFamily.SERIF, scroll = false, publisherStyles = false)
    private suspend fun saveCommit(locator: Locator) {
        val id = book?.id ?: return
        persistence.lock()
        try {
            storage.books.commit(id, locator.toJSON().toString())
            committed.value = locator
            Log.i("ReaderEvidence", "COMMITTED ${locator.toJSON()}")
        } finally { persistence.unlock() }
    }
    suspend fun refreshSelection(): Selection? = navigator.currentSelection().also {
        selection.value = it
        it?.let { Log.i("ReaderEvidence", "SELECTION ${it.locator.toJSON()}") }
    }
    fun clearSelection() { navigator.clearSelection(); selection.value = null }
    fun page(forward: Boolean) { if (bookNote.value != null || reflowing) return; if (!transient.value) preserveAnchor = false; if (forward) navigator.goForward() else navigator.goBackward() }
    suspend fun typography(font: Double, margins: Double) {
        if (transient.value || bookNote.value != null) return
        val anchor = committed.value ?: navigator.firstVisibleElementLocator() ?: return
        val record = book ?: return
        reflowing = true
        try {
            clearSelection()
            storage.typography(font, margins)
            saveCommit(anchor)
            // Recreate at the anchor so Readium's old-layout restoration cannot race app go().
            open(record.copy(committedLocator = anchor.toJSON().toString()), containerId, font, margins)
            awaitAnchorVisible(anchor)
            Log.i("ReaderEvidence", "REFLOW anchor=${anchor.toJSON()}")
        } finally { reflowing = false }
    }
    /** Set suppression before initiating movement, never after an engine callback. */
    fun preview(locator: Locator) { transient.value = true; clearSelection(); navigator.go(locator) }
    suspend fun cancelPreview() {
        val anchor = committed.value ?: return
        preserveAnchor = true
        navigator.go(anchor)
        awaitAnchorVisible(anchor)
        transient.value = false
    }
    private suspend fun awaitAnchorVisible(anchor: Locator) {
        val selector = anchor.locations.otherLocations["cssSelector"]?.toString() ?: return
        withTimeout(5000) {
            while (true) {
                val script = "(function(){var e=document.querySelector(" + JSONObject.quote(selector) + ");if(!e)return false;var r=e.getBoundingClientRect();return r.right>0 && r.left<innerWidth && r.bottom>0 && r.top<innerHeight})()"
                if (navigator.currentLocator.value.href == anchor.href && navigator.evaluateJavascript(script) == "true") break
                delay(100)
            }
        }
    }
    suspend fun commitPreview() {
        val destination = navigator.firstVisibleElementLocator() ?: visible.value ?: return
        saveCommit(destination)
        preserveAnchor = true
        transient.value = false
    }
    fun navigateCommitted(locator: Locator) { preserveAnchor = false; navigator.go(locator) }
    fun dismissBookNote() {
        Log.i("ReaderEvidence", "BOOK_NOTE_CLOSE target=${bookNote.value?.target} source=${committed.value?.toJSON()}")
        bookNote.value = null
    }
    fun diagnosticDestination(): Locator? = publication.readingOrder.lastOrNull()?.let { publication.locatorFromLink(it) }
}
