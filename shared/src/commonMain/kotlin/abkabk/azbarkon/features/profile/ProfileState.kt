package abkabk.azbarkon.features.profile

import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.model.profile.BadgeUi
import abkabk.azbarkon.domain.model.profile.GameProfileStats
import abkabk.azbarkon.domain.model.profile.LevelListItemUi
import abkabk.azbarkon.domain.model.profile.MemorizationProfileStats
import abkabk.azbarkon.domain.model.profile.ProfileLevelProgress
import abkabk.azbarkon.domain.model.profile.ProfileSheet
import androidx.compose.runtime.Stable

@Stable
data class ProfileState(
    val screenState: UiScreenState = UiScreenState.Idle,
    val activeSheet: ProfileSheet? = null,
    val themeMode: ThemeMode = ThemeMode.System,
    val fontSizeScale: Float = 1f,
    val isDailyDistichNotificationEnabled: Boolean = false,
    val isMemorizationReminderEnabled: Boolean = true,
    val isRemoteNotificationGranted: Boolean = false,
    val levelProgress: ProfileLevelProgress = ProfileLevelProgress(levelId = 1, levelName = "", currentXp = 0, targetXp = 900),
    val memorizationStats: MemorizationProfileStats = MemorizationProfileStats(),
    val gameStats: GameProfileStats = GameProfileStats(),
    val previewBadges: List<BadgeUi> = emptyList(),
    val allBadges: List<BadgeUi> = emptyList(),
    val allLevels: List<LevelListItemUi> = emptyList(),
    val reviewedVersesCount: Int = 0,
    val hasCompletedGhazal: Boolean = false,
    val pendingImportJson: String? = null,
)

sealed interface ProfileAction {
    data object OnLoad : ProfileAction

    data object OnSettingsClick : ProfileAction

    data object OnDismissSheet : ProfileAction

    data object OnViewAllBadgesClick : ProfileAction

    data object OnLevelsIconClick : ProfileAction

    data class OnDailyDistichNotificationToggle(
        val enabled: Boolean,
    ) : ProfileAction

    data class OnMemorizationReminderToggle(
        val enabled: Boolean,
    ) : ProfileAction

    data object OnRemoteNotificationClick : ProfileAction

    data class OnThemeModeSelected(
        val mode: ThemeMode,
    ) : ProfileAction

    data class OnFontSizeScaleSelected(
        val scale: Float,
    ) : ProfileAction

    data class OnNotificationPermissionResult(
        val granted: Boolean,
        val target: NotificationPermissionTarget,
    ) : ProfileAction

    data object OnExportData : ProfileAction

    data class OnImportDataSelected(
        val json: String,
    ) : ProfileAction

    data object OnConfirmImport : ProfileAction

    data object OnCancelImport : ProfileAction
}

sealed interface ProfileEvent {
    data class ShowSnackbar(
        val message: abkabk.azbarkon.core.uidata.UiText,
    ) : ProfileEvent

    data class RequestNotificationPermission(
        val target: NotificationPermissionTarget,
    ) : ProfileEvent

    data object OpenAppNotificationSettings : ProfileEvent
}

enum class NotificationPermissionTarget {
    DailyDistich,
    Remote,
}
