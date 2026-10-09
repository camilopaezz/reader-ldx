@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.reader.ldx

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun DictionaryPanel(
    dictionaries: List<DictionaryInfo>, selected: String, result: DictionaryMatch?, current: DictionaryInfo?,
    busy: Boolean, source: String, target: String,
    onLanguages: (String, String) -> Unit, onImport: () -> Unit, onLookup: (DictionaryInfo, String) -> Unit,
    onActions: (() -> Unit)?, onClose: () -> Unit
) {
    var input by remember(selected) { mutableStateOf(selected) }
    Surface(Modifier.fillMaxWidth().fillMaxHeight(0.65f), shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Offline dictionary", style = MaterialTheme.typography.titleLarge)
            Row {
                TextButton(onClick = onClose) { Text("Close lookup") }
                onActions?.let { TextButton(onClick = it) { Text("Passage actions") } }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (selected.isNotEmpty()) Text("Selected: $selected")
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Lookup text") }, modifier = Modifier.fillMaxWidth())
            Text("Installed sources. Book-language definitions appear first.", style = MaterialTheme.typography.bodySmall)
            dictionaries.forEach { dictionary ->
                FilledTonalButton(enabled = !busy, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth(), onClick = { onLookup(dictionary, input) }) { Text("${dictionary.name} [${dictionary.source} → ${dictionary.target}]") }
            }
            current?.let { Text("Source: ${it.name} [${it.source} → ${it.target}]") }
            result?.let {
                Text(it.kind)
                it.headword?.let { head -> Text("Matched headword: $head", style = MaterialTheme.typography.titleMedium) }
                it.definition?.let { definition -> Text(if (it.format == "m") definition else androidx.core.text.HtmlCompat.fromHtml(definition, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()) }
            }
            if (dictionaries.isEmpty()) Text("No dictionaries installed. Import a ZIP containing real StarDict files.")
            HorizontalDivider()
            Text("Import StarDict ZIP")
            Text("Language assignment is a prototype hypothesis. Assign the package's source and definition language.", style = MaterialTheme.typography.bodySmall)
            Row {
                TextButton(enabled = !busy, onClick = { onLanguages(if (source == "es") "en" else "es", target) }) { Text("Source: $source") }
                TextButton(enabled = !busy, onClick = { onLanguages(source, if (target == "es") "en" else "es") }) { Text("Target: $target") }
                TextButton(enabled = !busy, onClick = onImport) { Text("Choose ZIP") }
            }
        }
    }
}
