package dev.reader.ldx

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.readium.r2.shared.publication.Locator

@Composable fun SliderUi(controller: SliderController, current: Locator?, enabled: Boolean = true) {
    Text(if (controller.previewing) "Preview ${(100 * controller.value).toInt()}% · Tap page to read" else "Reading position ${(100 * controller.fraction(current)).toInt()}%", style = MaterialTheme.typography.labelMedium)
    Slider(value = if (controller.previewing) controller.value else controller.fraction(current),
        onValueChange = controller::preview, enabled = enabled && !controller.busy && controller.positions.isNotEmpty(), modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Reading position preview slider" })
}
