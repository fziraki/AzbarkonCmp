package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.notification_permission_allow
import sarv.shared.generated.resources.notification_permission_body
import sarv.shared.generated.resources.notification_permission_not_now
import sarv.shared.generated.resources.notification_permission_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

const val MAX_NOTIFICATION_PERMISSION_DECLINES = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionSheet(
    onDismiss: () -> Unit,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userPreferencesRepository: UserPreferencesRepository = koinInject()
    val requestNotificationPermission =
        rememberNotificationPermissionRequester { granted ->
            if (!granted) {
                userPreferencesRepository.incrementNotificationPermissionDeclineCount()
            }
            onResult(granted)
        }

    SarvModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = modifier.fillMaxWidth().padding(
                bottom = LocalSarvDimensions.current.dimen16,
                start = LocalSarvDimensions.current.dimen24,
                end = LocalSarvDimensions.current.dimen24
            ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.notification_permission_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.notification_permission_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12)) {

                SarvPrimaryButton(
                    text = stringResource(Res.string.notification_permission_allow),
                    onClick = requestNotificationPermission,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    shape = SarvButtonDefaults.Shape,
                    border = BorderStroke(
                        width = LocalSarvDimensions.current.dimen1,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    onClick = onDismiss
                ) {
                    Text(
                        text = stringResource(Res.string.notification_permission_not_now),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

        }
    }
}
