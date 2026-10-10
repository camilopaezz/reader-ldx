@file:OptIn(org.readium.r2.shared.ExperimentalReadiumApi::class)
package dev.reader.ldx

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.readium.r2.navigator.Selection
import java.io.File
import java.security.MessageDigest

/** Compose owns controls, Readium owns publication rendering and native selection handles. */
class MainActivity : AppCompatActivity() {
    lateinit var storage: ReaderStorage; private set
    lateinit var engine: ReadingEngine; private set
    lateinit var annotations: AnnotationController; private set
    private lateinit var slider: SliderController
    private val containerId = 1001
    private var books by mutableStateOf<List<BookRecord>>(emptyList())
    private var controls by mutableStateOf(false)
    private var topPanel by mutableStateOf<ReaderPanel?>(null)
    private var library by mutableStateOf(true)
    private var opened by mutableStateOf<BookRecord?>(null)
    private var message by mutableStateOf("")
    private var size by mutableStateOf(100.0)
    private var margins by mutableStateOf(1.0)
    private var selectionInfo by mutableStateOf<Selection?>(null)
    private lateinit var dictionaries: DictionaryStore
    private var installedDictionaries by mutableStateOf<List<DictionaryInfo>>(emptyList())
    private var dictionaryOpen by mutableStateOf(false)
    private var dictionarySelection by mutableStateOf<Selection?>(null)
    private var dictionaryResult by mutableStateOf<DictionaryMatch?>(null)
    private var currentDictionary by mutableStateOf<DictionaryInfo?>(null)
    private var dictionaryBusy by mutableStateOf(false)
    private var dictionarySource by mutableStateOf("es")
    private var dictionaryTarget by mutableStateOf("es")
    private val importDictionary = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { lifecycleScope.launch { dictionaryBusy = true; try { guarded {
            val staged = File(cacheDir, "dictionary.zip")
            val imported = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(it)?.use { input -> staged.outputStream().use { input.copyTo(it) } } ?: error("Unable to read dictionary")
                dictionaries.importPackage(staged, dictionarySource, dictionaryTarget)
            }
            installedDictionaries = withContext(Dispatchers.IO) { dictionaries.installed() }
            message = "Dictionary imported: ${imported.name}"
            android.util.Log.i("ReaderEvidence", "DICTIONARY_IMPORTED id=${imported.id} name=${imported.name} source=${imported.source} target=${imported.target}")
        } } finally { dictionaryBusy = false } } }
    }
    private var typographyJob: Job? = null
    private var applyingTypography by mutableStateOf(false)
    private var nativeSelectionMode: ActionMode? = null
    private var selectionModeCleanup: Job? = null
    private var initialLookupShown = false
    // Feature tickets add actions here while leaving selection acquisition owned by the engine.
    val selectionActions = linkedMapOf<String, (Selection) -> Unit>()
    var onWordSelected: ((Selection) -> Unit)? = null
    private val import = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { lifecycleScope.launch { guarded { importBook(it); books = storage.books.all() } } }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Process restart always constructs a fresh engine and discards transient fragment UI.
        super.onCreate(null)
        storage = ReaderStorage(this)
        engine = ReadingEngine(this, storage)
        slider = SliderController(storage, engine, lifecycleScope)
        engine.onPreviewPageTap = { slider.commitFromPage().also { if (it) controls = false } }
        dictionaries = DictionaryStore(File(filesDir, "dictionaries"))
        installedDictionaries = dictionaries.installed()
        onWordSelected = { selected -> showDictionary(selected) }
        selectionActions["Dictionary lookup"] = { selected -> selectionInfo = null; showDictionary(selected) }
        annotations = AnnotationController(AnnotationStore(this), engine)
        selectionActions["Highlight"] = { selection -> selectionInfo = null; annotations.begin(selection, false) }
        selectionActions["Annotation note"] = { selection -> selectionInfo = null; annotations.begin(selection, true) }
        engine.onCenterTap = { controls = !controls }
        engine.actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                selectionModeCleanup?.cancel()
                nativeSelectionMode = mode
                android.util.Log.i("ReaderEvidence", "SELECTION_MODE_CREATE initialLookupShown=$initialLookupShown")
                menu.clear()
                menu.add(0, 1, 0, "Passage actions")
                lifecycleScope.launch {
                    delay(100)
                    if (nativeSelectionMode !== mode) return@launch
                    engine.refreshSelection()?.let {
                        // WebView recreates ActionMode when a handle drag finishes. That
                        // is still the same selection, not another held word.
                        if (!initialLookupShown) {
                            initialLookupShown = true
                            onWordSelected?.invoke(it)
                        }
                    }
                }
                return true
            }
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                lifecycleScope.launch { dictionaryOpen = false; selectionInfo = engine.refreshSelection() }
                return true
            }
            override fun onDestroyActionMode(mode: ActionMode) {
                if (nativeSelectionMode !== mode) return
                nativeSelectionMode = null
                selectionInfo = null
                android.util.Log.i("ReaderEvidence", "SELECTION_MODE_DESTROY")
                selectionModeCleanup = lifecycleScope.launch {
                    // Allow native handle movement to replace the floating toolbar.
                    // End the episode only when the DOM range has actually gone away.
                    delay(150)
                    if (nativeSelectionMode == null && engine.refreshSelection() == null) {
                        initialLookupShown = false
                        android.util.Log.i("ReaderEvidence", "SELECTION_EPISODE_END")
                    }
                }
            }
        }
        val root = FrameLayout(this)
        // Reserve space in the native viewport, not in EPUB paragraphs: a paragraph
        // can continue in the next CSS column without carrying its top margin.
        // FrameLayout measures the navigator against this smaller height, so page
        // turns, selection and reflow share the same bounds on every page.
        val readingGutter = (24 * resources.displayMetrics.density).toInt()
        root.addView(FrameLayout(this).apply { id = containerId }, FrameLayout.LayoutParams(-1, -1).apply {
            topMargin = readingGutter
            bottomMargin = readingGutter
        })
        val overlay = ComposeView(this).apply { setContent { ReaderExpressiveTheme { ReaderUi() } } }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        setContentView(root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    topPanel != null -> topPanel = null
                    annotations.dismiss() -> Unit
                    engine.bookNote.value != null -> engine.backWithinBookNote()
                    dictionaryOpen -> dictionaryOpen = false
                    engine.transient.value -> lifecycleScope.launch { guarded { if (slider.previewing) slider.cancel() else engine.cancelPreview(); controls = false } }
                    nativeSelectionMode != null || selectionInfo != null -> { selectionInfo = null; nativeSelectionMode?.finish(); engine.clearSelection() }
                    controls -> controls = false
                    !library -> { library = true; controls = false }
                    else -> finish()
                }
            }
        })
        lifecycleScope.launch { guarded {
            storage.typography().let { size = it.first; margins = it.second }
            books = storage.books.all()
            storage.lastBook()?.let { id -> storage.books.get(id)?.let { open(it) } }
        } }
    }
    private suspend fun guarded(block: suspend () -> Unit) {
        try { block() } catch (e: Exception) { message = e.message ?: "Operation failed"; android.util.Log.e("ReaderEvidence", message, e) }
    }
    private suspend fun open(book: BookRecord) {
        selectionModeCleanup?.cancel()
        initialLookupShown = false
        engine.open(book, containerId, size, margins)
        slider.opened()
        opened = book; library = false; controls = false; topPanel = null; selectionInfo = null; dictionaryOpen = false
    }
    private suspend fun importBook(uri: Uri) {
        val staged = File(cacheDir, "import.epub")
        withContext(Dispatchers.IO) { contentResolver.openInputStream(uri)?.use { input -> staged.outputStream().use { input.copyTo(it) } } ?: error("Unable to read file") }
        importFile(staged)
    }
    private suspend fun importFixture(lang: String) {
        val staged = File(cacheDir, "fixture.epub")
        withContext(Dispatchers.IO) { assets.open("fixtures/${if (lang.startsWith("marked") || lang.startsWith("generic")) lang else "foundation-$lang"}.epub").use { input -> staged.outputStream().use { input.copyTo(it) } } }
        importFile(staged)
    }
    private suspend fun importFile(staged: File) {
        val id = withContext(Dispatchers.IO) { MessageDigest.getInstance("SHA-256").digest(staged.readBytes()).joinToString("") { "%02x".format(it) } }
        val directory = File(filesDir, "books").apply { mkdirs() }
        val owned = File(directory, "$id.epub")
        withContext(Dispatchers.IO) { staged.copyTo(owned, overwrite = true) }
        // Validate and obtain publication metadata before inserting an entry.
        val record = BookRecord(id, owned.path, "Imported EPUB", "und")
        engine.open(record, containerId, size, margins)
        val metadata = engine.publication.metadata
        val imported = record.copy(title = metadata.title ?: "Imported EPUB", language = metadata.languages.firstOrNull() ?: "und")
        storage.books.insert(imported)
        books = storage.books.all()
        open(storage.books.get(id)!!)
    }
    private fun sortedDictionaries(): List<DictionaryInfo> {
        val language = opened?.language?.substringBefore('-')
        return installedDictionaries.sortedBy { if (it.source == language && it.target == language) 0 else if (it.source == language) 1 else 2 }
    }
    private fun showDictionary(selected: Selection) {
        dictionarySelection = selected; dictionaryOpen = true; dictionaryResult = null
        sortedDictionaries().firstOrNull()?.let { lookupDictionary(it, selected.locator.text.highlight.orEmpty()) }
    }
    private fun lookupDictionary(info: DictionaryInfo, text: String) {
        lifecycleScope.launch { dictionaryBusy = true; try { guarded {
            currentDictionary = info
            val started = System.nanoTime()
            dictionaryResult = withContext(Dispatchers.IO) { dictionaries.lookup(info, text) }
            android.util.Log.i("ReaderEvidence", "LOOKUP selected=$text source=${info.source} target=${info.target} headword=${dictionaryResult?.headword} kind=${dictionaryResult?.kind} elapsedMs=${(System.nanoTime() - started) / 1_000_000}")
        } } finally { dictionaryBusy = false } }
    }
    private fun applyTypography() {
        if (applyingTypography) return
        applyingTypography = true
        typographyJob = lifecycleScope.launch {
            try { guarded { engine.typography(size, margins) } } finally { applyingTypography = false }
        }
    }
    @Composable private fun ReaderUi() {
        val visible by engine.visible.collectAsState()
        val committed by engine.committed.collectAsState()
        val preview by engine.transient.collectAsState()
        val bookNote by engine.bookNote.collectAsState()
        Box(Modifier.fillMaxSize()) {
            if (library) Surface(Modifier.fillMaxSize()) {
                Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Reader LDX prototype", style = MaterialTheme.typography.headlineSmall)
                    Text("Import an unencrypted reflowable EPUB. Copies and reading positions stay on this device.")
                    Button(onClick = { import.launch(arrayOf("application/epub+zip", "application/octet-stream")) }) { Text("Import EPUB") }
                    Row {
                        Button(onClick = { lifecycleScope.launch { guarded { importFixture("es") } } }) { Text("Import Spanish fixture") }
                    }
                    Button(onClick = { lifecycleScope.launch { guarded { importFixture("en") } } }) { Text("Import English fixture") }
                    TextButton(onClick = { lifecycleScope.launch { guarded { importFixture("marked-short") } } }) { Text("Short footnote fixture") }
                    TextButton(onClick = { lifecycleScope.launch { guarded { importFixture("marked-long") } } }) { Text("Long footnote fixture") }
                    TextButton(onClick = { lifecycleScope.launch { guarded { importFixture("generic-notes") } } }) { Text("Generic nested note fixture") }
                    TextButton(onClick = { dictionarySelection = null; dictionaryResult = null; dictionaryOpen = true }) { Text("Dictionaries") }
                    books.forEach { book -> TextButton(onClick = { lifecycleScope.launch { guarded { open(book) } } }) { Text("${book.title} [${book.language}]") } }
                }
            } else if (controls || preview) {
                ReaderChrome(
                    title = opened?.title.orEmpty(), preview = preview,
                    enabled = !applyingTypography, bookmarkEnabled = !annotations.busy,
                    returnLabel = slider.returnTarget?.let { "Return to ${(100 * slider.fraction(it)).toInt()}%" },
                    sliderBusy = slider.busy,
                    notice = listOf(slider.notice, annotations.notice).filter { it.isNotBlank() }.joinToString("\n"),
                    library = { library = true }, typography = { topPanel = ReaderPanel.Typography },
                    bookmark = { lifecycleScope.launch { guarded { annotations.bookmark() } } },
                    dictionaries = { dictionarySelection = null; dictionaryResult = null; dictionaryOpen = true },
                    annotations = { annotations.showList = true }, diagnostics = { topPanel = ReaderPanel.Diagnostics },
                    close = { controls = false },
                    cancel = { lifecycleScope.launch { guarded { slider.cancel() } } },
                    toggleReturn = { lifecycleScope.launch { guarded { slider.toggleReturn() } } })
                Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        SliderUi(slider, committed, enabled = !applyingTypography)
                    }
                }
            }
            when (topPanel) {
                ReaderPanel.Typography -> ReaderTypographyDialog(size, margins, !preview && !applyingTypography,
                    smaller = { size = (size - 20).coerceAtLeast(60.0); applyTypography() },
                    larger = { size = (size + 20).coerceAtMost(220.0); applyTypography() },
                    changeMargins = { margins = if (margins == 1.0) 2.0 else 1.0; applyTypography() },
                    dismiss = { topPanel = null })
                ReaderPanel.Diagnostics -> ReaderDiagnosticsDialog(
                    visible = "${visible?.locations?.otherLocations?.get("cssSelector") ?: visible?.href}\n${visible?.text?.highlight.orEmpty()}",
                    committed = "${committed?.locations?.otherLocations?.get("cssSelector") ?: committed?.href}",
                    enabled = !preview && !applyingTypography,
                    jump = { kind -> engine.diagnosticDestination()?.let { target ->
                        android.util.Log.i("ReaderEvidence", "DIAGNOSTIC_JUMP kind=$kind return=${slider.returnTarget?.toJSON()}")
                        engine.navigateCommitted(target)
                        topPanel = null
                    } }, dismiss = { topPanel = null })
                null -> Unit
            }
            selectionInfo?.let { selected ->
                Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), tonalElevation = 8.dp) {
                    Column(Modifier.heightIn(max = 480.dp).padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text(selected.locator.text.highlight.orEmpty())
                        selectionActions.forEach { (name, action) -> TextButton(onClick = { action(selected) }) { Text(name) } }
                        TextButton(onClick = { selectionInfo = null }) { Text("Back to handles") }
                        TextButton(onClick = { selectionInfo = null; engine.clearSelection() }) { Text("Dismiss selection") }
                    }
                }
            }
            bookNote?.let { BookNoteOverlay(it, engine.notePublication, engine::followBookNote, engine::backWithinBookNote, engine::dismissBookNote) }
            if (dictionaryOpen) Box(Modifier.align(Alignment.BottomCenter)) {
                DictionaryPanel(sortedDictionaries(), dictionarySelection?.locator?.text?.highlight.orEmpty(), dictionaryResult, currentDictionary,
                    dictionaryBusy, dictionarySource, dictionaryTarget, { source, target -> dictionarySource = source; dictionaryTarget = target },
                    { importDictionary.launch(arrayOf("application/zip", "application/octet-stream")) }, ::lookupDictionary,
                    dictionarySelection?.let { { lifecycleScope.launch { dictionaryOpen = false; selectionInfo = engine.refreshSelection() ?: dictionarySelection } } }, { dictionaryOpen = false })
            }
            if (message.isNotEmpty()) AlertDialog(onDismissRequest = { message = "" }, title = { Text(if (message.startsWith("Dictionary imported:")) "Dictionary imported" else "Reader error") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { message = "" }) { Text("Close") } })
            AnnotationUi(annotations, save = { record -> lifecycleScope.launch { guarded { annotations.save(record) } } },
                remove = { record -> lifecycleScope.launch { guarded { annotations.remove(record) } } })

        }
    }
}
