package dev.reader.ldx

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.readium.r2.shared.publication.Locator

/** Book-supplied content is transient and never becomes a reading destination. */
data class BookNote(val target: String, val html: String, val source: Locator?)

@Composable
fun BookNoteOverlay(note: BookNote, dismiss: () -> Unit) {
    var expanded by remember(note) { mutableStateOf(false) }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.94f).fillMaxHeight(if (expanded) .9f else .48f), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp)) {
                Text("Book note", style = MaterialTheme.typography.titleLarge)
                Row {
                    TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Collapse" else "Expand") }
                    TextButton(onClick = dismiss) { Text("Close note") }
                }
                AndroidView(modifier = Modifier.fillMaxWidth().weight(1f), factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.blockNetworkLoads = true
                        isVerticalScrollBarEnabled = true
                        // #5 supports demonstrated marked notes. Nested navigation belongs to #6.
                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest) = true
                        }
                        loadDataWithBaseURL(null, "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'/><style>body{font:20px Georgia,serif;line-height:1.5;color:#202020;background:white}p{margin:0 0 1em}</style></head><body>${note.html}</body></html>", "text/html", "UTF-8", null)
                    }
                })
                Text("Reading position stays at the source passage.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
