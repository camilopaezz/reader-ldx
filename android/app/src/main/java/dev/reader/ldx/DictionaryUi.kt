@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.reader.ldx

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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
            if (selected.isNotEmpty()) Text("Selected: $selected", maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (selected.isEmpty()) LookupInput(input, { input = it })
            DictionarySourcePicker(dictionaries, current, !busy) { onLookup(it, input) }
            result?.let {
                Text(it.kind, style = MaterialTheme.typography.labelMedium)
                it.headword?.let { head -> Text("Matched headword: $head", style = MaterialTheme.typography.titleMedium) }
                it.definition?.let { definition -> Text(if (it.format == "m") definition else androidx.core.text.HtmlCompat.fromHtml(definition, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()) }
            }
            if (selected.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Try another lookup", style = MaterialTheme.typography.titleSmall)
                LookupInput(input, { input = it })
                Text("Choose a source above to look up the edited text.", style = MaterialTheme.typography.bodySmall)
            }
            if (dictionaries.isEmpty()) Text("No dictionaries installed. Import a ZIP containing real StarDict files.")
            HorizontalDivider()
            Text("Import StarDict ZIP")
            Text("Language assignment is a prototype hypothesis. Assign the package's source and definition language.", style = MaterialTheme.typography.bodySmall)
            Row {
                TextButton(enabled = !busy, onClick = { onLanguages(if (source == "es") "en" else "es", target) }) { Text("Source: $source") }
                TextButton(enabled = !busy, onClick = { onLanguages(source, if (target == "es") "en" else "es") }) { Text("Target: $target") }
            }
            FilledTonalButton(enabled = !busy, shapes = ButtonDefaults.shapes(), onClick = onImport) { Text("Choose ZIP") }
        }
    }
}


@Composable private fun LookupInput(input: String, change: (String) -> Unit) {
    OutlinedTextField(value = input, onValueChange = change, label = { Text("Lookup text") }, modifier = Modifier.fillMaxWidth())
}

/** One source control keeps the selected word's definition visible without four stacked buttons. */
@Composable private fun DictionarySourcePicker(dictionaries: List<DictionaryInfo>, current: DictionaryInfo?, enabled: Boolean, lookup: (DictionaryInfo) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = current?.let { "${it.name}, ${it.source} to ${it.target}" } ?: "Choose installed dictionary"
    Box(Modifier.fillMaxWidth()) {
        FilledTonalButton(enabled = enabled && dictionaries.isNotEmpty(), shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Change dictionary source. $label" },
            onClick = { expanded = true }) {
            Text(current?.name ?: "Choose dictionary", Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(8.dp))
            current?.let { Text("${it.source} → ${it.target}", style = MaterialTheme.typography.labelLarge) }
            Text(" ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            dictionaries.forEach { dictionary ->
                DropdownMenuItem(text = {
                    Column {
                        Text(dictionary.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${dictionary.source} → ${dictionary.target}", style = MaterialTheme.typography.labelMedium)
                    }
                }, onClick = { expanded = false; lookup(dictionary) })
            }
        }
    }
}
