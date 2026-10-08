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
data class BookNote(val target: String, val html: String, val source: Locator?, val sourceTarget: String = "", val depth: Int = 0)

@Composable
fun BookNoteOverlay(note: BookNote, resources: BookNotePublication?, follow: (String) -> Unit, back: () -> Unit, dismiss: () -> Unit) {
    var expanded by remember(note) { mutableStateOf(false) }
    Dialog(onDismissRequest = back, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth(.94f).fillMaxHeight(if (expanded) .9f else .48f), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp)) {
                Text("Book note ${note.depth + 1}", style = MaterialTheme.typography.titleLarge)
                Row {
                    if (note.depth > 0) TextButton(onClick = back) { Text("Back within note") }
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
                        // Only publication resources are served; network and active content stay disabled.
                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean { follow(request.url.toString()); return true }
                            override fun shouldInterceptRequest(view: WebView, request: android.webkit.WebResourceRequest): android.webkit.WebResourceResponse? {
                                val uri = request.url
                                if (uri.host != "publication.invalid") return android.webkit.WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream(byteArrayOf()))
                                val path = uri.path.orEmpty().removePrefix("/")
                                val bytes = resources?.resource(path) ?: byteArrayOf()
                                val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(path.substringAfterLast('.')) ?: "application/octet-stream"
                                android.util.Log.i("ReaderEvidence", "NOTE_RESOURCE path=$path bytes=${bytes.size}")
                                return android.webkit.WebResourceResponse(mime, null, java.io.ByteArrayInputStream(bytes))
                            }
                        }

                    }
                }, update = { web ->
                    if (web.tag != note.target) {
                        web.tag = note.target
                        web.loadDataWithBaseURL("https://publication.invalid/${note.target.substringBefore('#')}", "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'/><style>body{font:20px Georgia,serif;line-height:1.5;color:#202020;background:white}p{margin:0 0 1em}img{max-width:100%;height:auto}</style></head><body>${note.html}</body></html>", "text/html", "UTF-8", null)
                    }
                })
                Text("Reading position stays at the source passage.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
