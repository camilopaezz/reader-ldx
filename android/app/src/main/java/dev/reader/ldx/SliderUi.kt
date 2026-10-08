package dev.reader.ldx

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.readium.r2.shared.publication.Locator

@Composable fun SliderUi(controller: SliderController, current: Locator?, cancel: () -> Unit, toggleReturn: () -> Unit) {
    if (controller.notice.isNotBlank()) Text(controller.notice)
    Text(if (controller.previewing) "Preview: tap the reading page to commit" else "Reading position ${(100 * controller.fraction(current)).toInt()}%")
    Slider(value = if (controller.previewing) controller.value else controller.fraction(current),
        onValueChange = controller::preview, enabled = !controller.busy && controller.positions.isNotEmpty(), modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Reading position preview slider" })
    Row {
        if (controller.previewing) TextButton(enabled = !controller.busy, onClick = cancel) { Text("Cancel preview") }
        controller.returnTarget?.let { target ->
            TextButton(enabled = !controller.previewing && !controller.busy, onClick = toggleReturn) {
                Text("Return to ${(100 * controller.fraction(target)).toInt()}%")
            }
        }
    }
}
