package abkabk.azbarkon.features.home

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.util.Constants
import abkabk.azbarkon.features.profile.util.platformName
import abkabk.azbarkon.ui.components.SarvButtonDefaults
import abkabk.azbarkon.ui.components.SarvModalBottomSheet
import abkabk.azbarkon.ui.components.SarvPrimaryButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.update_button
import sarv.shared.generated.resources.update_later
import sarv.shared.generated.resources.update_optional_body
import sarv.shared.generated.resources.update_optional_title
import sarv.shared.generated.resources.update_required_body
import sarv.shared.generated.resources.update_required_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    updateType: UpdateType,
    properties: ModalBottomSheetProperties,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    val storeUrl = if (platformName() == "android") {
        Constants.ANDROID_STORE_URL
    } else {
        Constants.IOS_STORE_URL
    }

    SarvModalBottomSheet(
        onDismissRequest = if (updateType == UpdateType.MANDATORY) {{}} else onDismiss,
        sheetGesturesEnabled = updateType == UpdateType.OPTIONAL,
        properties = properties,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = LocalSarvDimensions.current.dimen16,
                    start = LocalSarvDimensions.current.dimen24,
                    end = LocalSarvDimensions.current.dimen24
                ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16)
        ) {
            Text(
                text = stringResource(
                    if (updateType == UpdateType.MANDATORY) {
                        Res.string.update_required_title
                    } else {
                        Res.string.update_optional_title
                    },
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(
                    if (updateType == UpdateType.MANDATORY) {
                        Res.string.update_required_body
                    } else {
                        Res.string.update_optional_body
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12)) {

                SarvPrimaryButton(
                    text = stringResource(Res.string.update_button),
                    onClick = {
                        scope.launch {
                            uriHandler.openUri(storeUrl)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                if (updateType == UpdateType.OPTIONAL) {

                    TextButton(
                        modifier = Modifier.weight(1f),
                        shape = SarvButtonDefaults.Shape,
                        border = BorderStroke(width = LocalSarvDimensions.current.dimen1,
                            color = MaterialTheme.colorScheme.primary),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(Res.string.update_later),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

        }
    }
}

