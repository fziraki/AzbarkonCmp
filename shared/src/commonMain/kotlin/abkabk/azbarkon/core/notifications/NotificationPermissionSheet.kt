package abkabk.azbarkon.core.notifications

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import abkabk.azbarkon.ui.components.SarvModalBottomSheet
import abkabk.azbarkon.ui.components.SarvPrimaryButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.notification_permission_allow
import sarv.shared.generated.resources.notification_permission_body
import sarv.shared.generated.resources.notification_permission_not_now
import sarv.shared.generated.resources.notification_permission_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

const val MAX_NOTIFICATION_PERMISSION_DECLINES = 2

@Composable
fun NotificationPermissionSheet(
    onDismiss: () -> Unit,
    onResult: (Boolean) -> Unit,
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
            modifier = Modifier.fillMaxWidth().padding(LocalSarvDimensions.current.dimen24),
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
            SarvPrimaryButton(
                text = stringResource(Res.string.notification_permission_allow),
                onClick = requestNotificationPermission,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(Res.string.notification_permission_not_now),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
