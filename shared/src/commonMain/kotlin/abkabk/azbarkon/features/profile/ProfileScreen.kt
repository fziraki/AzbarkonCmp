package abkabk.azbarkon.features.profile

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSarvAppState
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.core.uidata.asString
import abkabk.azbarkon.core.notifications.NotificationPermissionSheet
import abkabk.azbarkon.core.notifications.openAppNotificationSettings
import abkabk.azbarkon.core.notifications.rememberNotificationPermissionRequester
import abkabk.azbarkon.features.profile.util.rememberBackupImportLauncher
import abkabk.azbarkon.features.profile.util.showToast
import abkabk.azbarkon.features.profile.util.versionName
import abkabk.azbarkon.ui.components.SarvAlertDialog
import abkabk.azbarkon.ui.theme.SarvTheme
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.clear_cancel
import sarv.shared.generated.resources.clear_confirm
import sarv.shared.generated.resources.profile_import_confirm_body
import sarv.shared.generated.resources.profile_import_confirm_title
import sarv.shared.generated.resources.profile_version
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val appState = LocalSarvAppState.current
    var snackbarMessage by remember { mutableStateOf<abkabk.azbarkon.core.uidata.UiText?>(null) }
    var showRemotePermissionSheet by remember { mutableStateOf(false) }
    val requestNotificationPermission =
        rememberNotificationPermissionRequester { granted ->
            viewModel.onAction(
                ProfileAction.OnNotificationPermissionResult(
                    granted,
                    NotificationPermissionTarget.DailyDistich,
                ),
            )
        }
    val openImportPicker =
        rememberBackupImportLauncher { json ->
            if (json != null) {
                viewModel.onAction(ProfileAction.OnImportDataSelected(json))
            }
        }

    DisposableEffect(viewModel) {
        appState.onProfileSettingsClick = {
            viewModel.onAction(ProfileAction.OnSettingsClick)
        }
        onDispose {
            appState.onProfileSettingsClick = null
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ProfileEvent.ShowSnackbar -> {
                snackbarMessage = event.message
            }

            is ProfileEvent.RequestNotificationPermission -> {
                when (event.target) {
                    NotificationPermissionTarget.DailyDistich -> requestNotificationPermission()
                    NotificationPermissionTarget.Remote -> showRemotePermissionSheet = true
                }
            }

            ProfileEvent.OpenAppNotificationSettings -> {
                openAppNotificationSettings()
            }
        }
    }

    val resolvedSnackbarMessage = snackbarMessage?.asString()
    LaunchedEffect(resolvedSnackbarMessage) {
        if (resolvedSnackbarMessage != null) {
            showToast(resolvedSnackbarMessage)
            snackbarMessage = null
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        ProfileScreen(
            state = state,
            onAction = viewModel::onAction,
        )
        ProfileSheets(
            state = state,
            onAction = viewModel::onAction,
            onExportData = { viewModel.onAction(ProfileAction.OnExportData) },
            onImportData = openImportPicker,
        )
    }

    if (state.pendingImportJson != null) {
        SarvAlertDialog(
            onDismissRequest = { viewModel.onAction(ProfileAction.OnCancelImport) },
            title = stringResource(Res.string.profile_import_confirm_title),
            text = stringResource(Res.string.profile_import_confirm_body),
            confirmLabel = stringResource(Res.string.clear_confirm),
            onConfirm = { viewModel.onAction(ProfileAction.OnConfirmImport) },
            dismissLabel = stringResource(Res.string.clear_cancel),
        )
    }

    if (showRemotePermissionSheet) {
        NotificationPermissionSheet(
            onDismiss = { showRemotePermissionSheet = false },
            onResult = { granted ->
                viewModel.onAction(
                    ProfileAction.OnNotificationPermissionResult(
                        granted,
                        NotificationPermissionTarget.Remote,
                    ),
                )
                showRemotePermissionSheet = false
            },
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpanded) {
        Row(
            modifier = modifier.fillMaxSize().padding(LocalSarvDimensions.current.dimen16),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            ) {
                item {
                    ProfileHeader(
                        levelProgress = state.levelProgress,
                        onLevelsClick = { onAction(ProfileAction.OnLevelsIconClick) },
                    )
                }

                item {
                    ProfileBadges(
                        badges = state.previewBadges,
                        onViewAllClick = { onAction(ProfileAction.OnViewAllBadgesClick) },
                    )
                }

                item {
                    Text(
                        text = stringResource(Res.string.profile_version, versionName()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = LocalSarvDimensions.current.dimen8),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            ) {
                item {
                    GameStatusCard(stats = state.gameStats)
                }

                item {
                    MemorizationStatusCard(stats = state.memorizationStats)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(LocalSarvDimensions.current.dimen16),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            item {
                ProfileHeader(
                    levelProgress = state.levelProgress,
                    onLevelsClick = { onAction(ProfileAction.OnLevelsIconClick) },
                )
            }

            item {
                GameStatusCard(stats = state.gameStats)
            }

            item {
                MemorizationStatusCard(stats = state.memorizationStats)
            }

            item {
                ProfileBadges(
                    badges = state.previewBadges,
                    onViewAllClick = { onAction(ProfileAction.OnViewAllBadgesClick) },
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.profile_version, versionName()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize().padding(top = LocalSarvDimensions.current.dimen8),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    SarvTheme {
        ProfileScreen(
            state = ProfileState(),
            onAction = {},
        )
    }
}
