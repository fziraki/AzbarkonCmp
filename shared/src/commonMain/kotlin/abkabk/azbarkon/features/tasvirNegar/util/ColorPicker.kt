package abkabk.azbarkon.features.tasvirNegar.util

import abkabk.azbarkon.ui.components.SarvButton
import abkabk.azbarkon.ui.components.SarvModalBottomSheet
import abkabk.azbarkon.ui.components.SarvPrimaryButton
import abkabk.azbarkon.ui.components.SarvSlider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.clear_cancel
import sarv.shared.generated.resources.tasvir_pick_color
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val LABEL_WEIGHT = 0.2f
private const val SLIDER_WEIGHT = 0.8f

@Composable
expect fun TasvirCustomColorPicker(
    visible: Boolean,
    onDismiss: () -> Unit,
    onColorSelect: (Color) -> Unit,
)

@Composable
internal fun HsvColorPickerContent(
    onDismiss: () -> Unit,
    onColorSelect: (Color) -> Unit,
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    val selectedColor = remember(hue, saturation, value) { Color.hsv(hue, saturation, value) }

    SarvModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = false,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LocalSarvDimensions.current.dimen16)
                    .padding(bottom = LocalSarvDimensions.current.dimen24),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            Text(
                text = stringResource(Res.string.tasvir_pick_color),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(LocalSarvDimensions.current.dimen48)
                        .background(selectedColor, RoundedCornerShape(LocalSarvDimensions.current.dimen8)),
            )
            ColorSlider(label = "رنگ", value = hue, valueRange = 0f..360f) { hue = it }
            ColorSlider(label = "اشباع", value = saturation) { saturation = it }
            ColorSlider(label = "روشنایی", value = value) { value = it }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
            ) {
                SarvButton(
                    text = stringResource(Res.string.clear_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                )
                SarvPrimaryButton(
                    text = stringResource(Res.string.tasvir_pick_color),
                    onClick = { onColorSelect(selectedColor) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(LABEL_WEIGHT),
            style = MaterialTheme.typography.labelMedium,
        )
        SarvSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(SLIDER_WEIGHT),
        )
    }
}

