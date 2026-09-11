@file:OptIn(ExperimentalMaterial3Api::class)

package abkabk.azbarkon.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SarvModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetGesturesEnabled: Boolean = true,
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetGesturesEnabled = sheetGesturesEnabled,
        properties = properties,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        content = content,
    )
}
