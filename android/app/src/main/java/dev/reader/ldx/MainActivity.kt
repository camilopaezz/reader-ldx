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
    private val containerId = 1001
    private var books by mutableStateOf<List<BookRecord>>(emptyList())
    private var controls by mutableStateOf(false)
    private var library by mutableStateOf(true)
    private var opened by mutableStateOf<BookRecord?>(null)
    private var message by mutableStateOf("")
    private var size by mutableStateOf(100.0)
    private var margins by mutableStateOf(1.0)
    private var selectionInfo by mutableStateOf<Selection?>(null)
    private var typographyJob: Job? = null
    private var applyingTypography by mutableStateOf(false)
    private var nativeSelectionMode: ActionMode? = null
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
        engine.onCenterTap = { controls = !controls }
        engine.actionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                nativeSelectionMode = mode
                menu.clear()
                menu.add(0, 1, 0, "Passage actions")
                lifecycleScope.launch { delay(100); engine.refreshSelection()?.let { onWordSelected?.invoke(it) } }
                return true
            }
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                lifecycleScope.launch { selectionInfo = engine.refreshSelection() }
                return true
            }
            override fun onDestroyActionMode(mode: ActionMode) { nativeSelectionMode = null; selectionInfo = null; engine.selection.value = null }
        }
        val root = FrameLayout(this)
        root.addView(FrameLayout(this).apply { id = containerId }, FrameLayout.LayoutParams(-1, -1))
        val overlay = ComposeView(this).apply { setContent { MaterialTheme { ReaderUi() } } }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        setContentView(root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    engine.bookNote.value != null -> engine.dismissBookNote()
                    engine.transient.value -> lifecycleScope.launch { engine.cancelPreview(); controls = false }
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
        engine.open(book, containerId, size, margins)
        opened = book; library = false; controls = false; selectionInfo = null
    }
    private suspend fun importBook(uri: Uri) {
        val staged = File(cacheDir, "import.epub")
        withContext(Dispatchers.IO) { contentResolver.openInputStream(uri)?.use { input -> staged.outputStream().use { input.copyTo(it) } } ?: error("Unable to read file") }
        importFile(staged)
    }
    private suspend fun importFixture(lang: String) {
        val staged = File(cacheDir, "fixture.epub")
        withContext(Dispatchers.IO) { assets.open("fixtures/${if (lang.startsWith("marked")) lang else "foundation-$lang"}.epub").use { input -> staged.outputStream().use { input.copyTo(it) } } }
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
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Reader LDX prototype", style = MaterialTheme.typography.headlineSmall)
                    Text("Import an unencrypted reflowable EPUB. Copies and reading positions stay on this device.")
                    Button(onClick = { import.launch(arrayOf("application/epub+zip", "application/octet-stream")) }) { Text("Import EPUB") }
                    Row {
                        Button(onClick = { lifecycleScope.launch { guarded { importFixture("es") } } }) { Text("Import Spanish fixture") }
                    }
                    Button(onClick = { lifecycleScope.launch { guarded { importFixture("en") } } }) { Text("Import English fixture") }
                    Row {
                        TextButton(onClick = { lifecycleScope.launch { guarded { importFixture("marked-short") } } }) { Text("Short footnote fixture") }
                        TextButton(onClick = { lifecycleScope.launch { guarded { importFixture("marked-long") } } }) { Text("Long footnote fixture") }
                    }
                    books.forEach { book -> TextButton(onClick = { lifecycleScope.launch { guarded { open(book) } } }) { Text("${book.title} [${book.language}]") } }
                }
            } else if (controls || preview) {
                Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), tonalElevation = 8.dp) {
                    Column(Modifier.padding(12.dp)) {
                        Text(opened?.title.orEmpty(), style = MaterialTheme.typography.titleSmall)
                        Text(if (preview) "Diagnostic preview, reading position unchanged" else "Committed reading")
                        Text("Anchor: ${visible?.locations?.otherLocations?.get("cssSelector") ?: visible?.href}", style = MaterialTheme.typography.bodySmall)
                        Text(visible?.text?.highlight?.take(85).orEmpty(), style = MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(enabled = !applyingTypography, onClick = { engine.page(false) }) { Text("Previous") }
                            TextButton(enabled = !applyingTypography, onClick = { engine.page(true) }) { Text("Next") }
                            TextButton(onClick = { library = true }) { Text("Fixtures") }
                            TextButton(onClick = { controls = false }) { Text("Close") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Aa ${size.toInt()}%")
                            TextButton(enabled = !preview && !applyingTypography, onClick = { size = (size - 20).coerceAtLeast(60.0); applyTypography() }) { Text("Smaller") }
                            TextButton(enabled = !preview && !applyingTypography, onClick = { size = (size + 20).coerceAtMost(220.0); applyTypography() }) { Text("Larger") }
                            TextButton(enabled = !preview && !applyingTypography, onClick = { margins = if (margins == 1.0) 2.0 else 1.0; applyTypography() }) { Text("Margins") }
                        }
                        if (preview) TextButton(onClick = { lifecycleScope.launch { engine.cancelPreview() } }) { Text("Cancel preview") }
                        else TextButton(onClick = { engine.diagnosticDestination()?.let { engine.preview(it) } }) { Text("Diagnostic non-committing move") }
                        Text("Saved: ${committed?.locations?.otherLocations?.get("cssSelector") ?: committed?.href}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            selectionInfo?.let { selected ->
                Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), tonalElevation = 8.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Text(selected.locator.text.highlight.orEmpty())
                        Text("${selected.locator.href} ${selected.locator.locations}", style = MaterialTheme.typography.bodySmall)
                        selectionActions.forEach { (name, action) -> TextButton(onClick = { action(selected) }) { Text(name) } }
                        TextButton(onClick = { selectionInfo = null }) { Text("Back to handles") }
                        TextButton(onClick = { selectionInfo = null; engine.clearSelection() }) { Text("Dismiss selection") }
                    }
                }
            }
            bookNote?.let { BookNoteOverlay(it, engine::dismissBookNote) }
            if (message.isNotEmpty()) AlertDialog(onDismissRequest = { message = "" }, title = { Text("Reader error") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { message = "" }) { Text("Close") } })
        }
    }
}
