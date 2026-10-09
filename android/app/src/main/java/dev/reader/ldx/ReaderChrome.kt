@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.reader.ldx

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** App-owned chrome uses the actual Expressive theme, motion and morphing button APIs. */
@Composable
fun ReaderExpressiveTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(motionScheme = MotionScheme.expressive(), content = content)
}

enum class ReaderPanel { Typography, Diagnostics }

// Small original vector drawings avoid importing the deprecated Material icons catalogue.
private fun readerIcon(name: String, draw: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, pathBuilder = draw)
    }.build()
private val LibraryIcon = readerIcon("Library") {
    moveTo(4f, 4f); lineTo(4f, 20f); lineTo(20f, 20f); lineTo(20f, 4f); close()
    moveTo(8f, 4f); lineTo(8f, 20f)
    moveTo(11f, 8f); lineTo(17f, 8f)
}
private val BookmarkIcon = readerIcon("Bookmark") {
    moveTo(6f, 3f); lineTo(18f, 3f); lineTo(18f, 21f); lineTo(12f, 17f); lineTo(6f, 21f); close()
}
private val CloseIcon = readerIcon("Close") {
    moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f)
}
private val MoreIcon = readerIcon("More") {
    moveTo(11f, 5f); lineTo(13f, 5f); moveTo(11f, 12f); lineTo(13f, 12f); moveTo(11f, 19f); lineTo(13f, 19f)
}

@Composable private fun ChromeButton(label: String, enabled: Boolean = true, click: () -> Unit, content: @Composable () -> Unit) {
    FilledTonalIconButton(onClick = click, enabled = enabled, shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.size(48.dp).semantics { contentDescription = label }, content = content)
}

@Composable
fun ReaderChrome(
    title: String, preview: Boolean, enabled: Boolean, bookmarkEnabled: Boolean,
    returnLabel: String?, sliderBusy: Boolean, notice: String,
    library: () -> Unit, typography: () -> Unit, bookmark: () -> Unit, dictionaries: () -> Unit,
    annotations: () -> Unit, diagnostics: () -> Unit, close: () -> Unit,
    cancel: () -> Unit, toggleReturn: () -> Unit,
) {
    var more by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(title, Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        HorizontalFloatingToolbar(expanded = true, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp)) {
            ChromeButton("Library", enabled && !preview, library) { Icon(LibraryIcon, contentDescription = null) }
            Spacer(Modifier.weight(1f))
            ChromeButton("Typography", enabled && !preview, typography) { Text("Aa", style = MaterialTheme.typography.titleMedium) }
            ChromeButton("Add bookmark", enabled && !preview && bookmarkEnabled, bookmark) { Icon(BookmarkIcon, contentDescription = null) }
            Box {
                ChromeButton("More reader options", enabled && !preview, { more = true }) { Icon(MoreIcon, contentDescription = null) }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text("Annotations") }, onClick = { more = false; annotations() })
                    DropdownMenuItem(text = { Text("Dictionaries") }, onClick = { more = false; dictionaries() })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Prototype diagnostics") }, onClick = { more = false; diagnostics() })
                }
            }
            ChromeButton("Close controls", !preview, close) { Icon(CloseIcon, contentDescription = null) }
        }
        if (preview || returnLabel != null || notice.isNotBlank()) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                    if (preview) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Preview", Modifier.weight(1f).padding(horizontal = 8.dp), style = MaterialTheme.typography.labelLarge)
                            FilledTonalButton(onClick = cancel, enabled = enabled && !sliderBusy, shapes = ButtonDefaults.shapes()) { Text("Cancel preview") }
                        }
                    } else returnLabel?.let {
                        FilledTonalButton(onClick = toggleReturn, enabled = enabled && !sliderBusy, shapes = ButtonDefaults.shapes()) { Text(it) }
                    }
                    if (notice.isNotBlank()) Text(notice, Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ReaderTypographyDialog(size: Double, margins: Double, enabled: Boolean, smaller: () -> Unit, larger: () -> Unit, changeMargins: () -> Unit, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Typography") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Text size ${size.toInt()}%", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = smaller, enabled = enabled && size > 60, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Smaller") }
                FilledTonalButton(onClick = larger, enabled = enabled && size < 220, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Larger") }
            }
            Text("Margins", style = MaterialTheme.typography.titleMedium)
            FilledTonalButton(onClick = changeMargins, enabled = enabled, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) {
                Text(if (margins == 1.0) "Use wider margins" else "Use narrower margins")
            }
            Text("Changes reflow the text while keeping your passage.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text("Done") } })
}

@Composable
fun ReaderDiagnosticsDialog(visible: String, committed: String, enabled: Boolean, jump: (String) -> Unit, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Prototype diagnostics") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Visible anchor\n$visible", style = MaterialTheme.typography.bodySmall)
            Text("Saved anchor\n$committed", style = MaterialTheme.typography.bodySmall)
            Text("Ordinary navigation to the last chapter. These actions do not add slider Return history.")
            listOf("Chapter", "Search result", "Bookmark style").forEach { kind ->
                TextButton(enabled = enabled, onClick = { jump(kind) }) { Text(kind) }
            }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text("Close diagnostics") } })
}
