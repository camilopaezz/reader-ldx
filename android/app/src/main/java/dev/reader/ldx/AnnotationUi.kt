package dev.reader.ldx

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun AnnotationUi(controller: AnnotationController, save: (AnnotationRecord) -> Unit, remove: (AnnotationRecord) -> Unit) {
    if (controller.showList) AlertDialog(
        onDismissRequest = { controller.showList = false },
        title = { Text("Annotations by chapter") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                if (controller.records.isEmpty()) Text("No annotations in this book")
                controller.records.groupBy { it.chapter }.forEach { (chapter, entries) ->
                    Text(chapter, style = MaterialTheme.typography.titleMedium)
                    entries.forEach { record ->
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        Text(if (record.kind == "bookmark") "Bookmark" else "${record.color} highlight", style = MaterialTheme.typography.labelLarge)
                        Text(record.excerpt.take(240))
                        if (record.note.isNotBlank()) Text(record.note)
                        Row {
                            TextButton(onClick = { controller.navigate(record) }) { Text("Open passage") }
                            if (record.kind == "passage") TextButton(onClick = { controller.editing = record }) { Text("Edit") }
                            TextButton(enabled = !controller.busy, onClick = { remove(record) }) { Text("Remove") }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { controller.showList = false }) { Text("Close list") } }
    )
    controller.editing?.let { record ->
        var text by remember(record.id, record.note) { mutableStateOf(record.note) }
        var color by remember(record.id, record.color) { mutableStateOf(record.color) }
        AlertDialog(
            onDismissRequest = { if (!controller.busy) controller.editing = null },
            title = { Text("Passage annotation") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(record.chapter, style = MaterialTheme.typography.labelLarge)
                    Text(record.excerpt.take(500))
                    Row { AnnotationController.colors.forEach { choice ->
                        TextButton(enabled = !controller.busy, onClick = { color = choice }) { Text(if (color == choice) "✓ $choice" else choice) }
                    } }
                    OutlinedTextField(value = text, onValueChange = { text = it }, enabled = !controller.busy,
                        label = { Text("Annotation note") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Text("Save stores the highlight and note on this device.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(enabled = !controller.busy, onClick = { save(record.copy(note = text, color = color)) }) { Text(if (controller.busy) "Saving…" else "Save") } },
            dismissButton = {
                Row {
                    if (controller.records.any { it.id == record.id }) TextButton(enabled = !controller.busy, onClick = { remove(record) }) { Text("Remove") }
                    TextButton(enabled = !controller.busy, onClick = { controller.editing = null }) { Text("Cancel") }
                }
            }
        )
    }
}
