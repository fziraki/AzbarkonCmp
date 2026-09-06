package abkabk.azbarkon.features.profile

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.notifications.MAX_NOTIFICATION_PERMISSION_DECLINES
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.model.profile.BadgeCatalog
import abkabk.azbarkon.domain.model.profile.GameLevelCatalog
import abkabk.azbarkon.domain.model.profile.LevelListItemUi
import abkabk.azbarkon.domain.model.profile.MemorizationProfileStats
import abkabk.azbarkon.domain.model.profile.ProfileSheet
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.platform.NotificationPermissionGateway
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import abkabk.azbarkon.domain.usecase.BuildProfileStatsUseCase
import abkabk.azbarkon.domain.usecase.ExportUserDataUseCase
import abkabk.azbarkon.domain.usecase.ImportUserDataUseCase
import abkabk.azbarkon.features.poets.GHAZAL_CATEGORY
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.profile_export_failed
import sarv.shared.generated.resources.profile_export_success
import sarv.shared.generated.resources.profile_import_failed
import sarv.shared.generated.resources.profile_import_success
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val memorizationRepository: MemorizationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dailyDistichNotificationScheduler: DailyDistichNotificationScheduler,
    private val notificationPermissionGateway: NotificationPermissionGateway,
    private val memorizationReviewNotificationCoordinator: MemorizationReviewNotificationCoordinator,
    private val buildProfileStats: BuildProfileStatsUseCase,
    private val exportUserData: ExportUserDataUseCase,
    private val importUserData: ImportUserDataUseCase,
) : BaseViewModel<ProfileAction, ProfileState, ProfileEvent>(
        initialState =
            ProfileState(
                isDailyDistichNotificationEnabled =
                    userPreferencesRepository.isDailyDistichNotificationEnabled(),
                isMemorizationReminderEnabled =
                    userPreferencesRepository.isMemorizationReminderEnabled(),
                isRemoteNotificationGranted =
                    notificationPermissionGateway.areNotificationsEnabled(),
                themeMode = userPreferencesRepository.getThemeMode(),
                fontSizeScale = userPreferencesRepository.getFontSizeScale(),
            ),
    ) {
    init {
        onAction(ProfileAction.OnLoad)
        observeProfileData()
    }

    override fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLoad,
            -> setState { copy(screenState = UiScreenState.Success) }

            ProfileAction.OnSettingsClick -> {
                setState {
                    copy(
                        activeSheet = ProfileSheet.Settings,
                        isRemoteNotificationGranted =
                            notificationPermissionGateway.areNotificationsEnabled(),
                    )
                }
            }

            ProfileAction.OnDismissSheet -> {
                setState { copy(activeSheet = null) }
            }

            ProfileAction.OnViewAllBadgesClick -> {
                setState { copy(activeSheet = ProfileSheet.Badges) }
            }

            ProfileAction.OnLevelsIconClick -> {
                setState { copy(activeSheet = ProfileSheet.Levels) }
            }

            is ProfileAction.OnDailyDistichNotificationToggle -> {
                if (action.enabled) {
                    enableDailyDistichNotifications()
                } else {
                    disableDailyDistichNotifications()
                }
            }

            is ProfileAction.OnMemorizationReminderToggle -> {
                setMemorizationReminder(action.enabled)
            }

            is ProfileAction.OnRemoteNotificationClick -> {
                enableRemoteNotifications()
            }

            is ProfileAction.OnThemeModeSelected -> {
                userPreferencesRepository.setThemeMode(action.mode)
                setState { copy(themeMode = action.mode) }
            }

            is ProfileAction.OnFontSizeScaleSelected -> {
                userPreferencesRepository.setFontSizeScale(action.scale)
                setState { copy(fontSizeScale = action.scale) }
            }

            is ProfileAction.OnNotificationPermissionResult -> {
                handleNotificationPermissionResult(action.granted, action.target)
            }

            ProfileAction.OnExportData -> {
                exportData()
            }

            is ProfileAction.OnImportDataSelected -> {
                setState { copy(pendingImportJson = action.json) }
            }

            ProfileAction.OnConfirmImport -> {
                state.value.pendingImportJson?.let { json ->
                    setState { copy(pendingImportJson = null) }
                    importData(json)
                }
            }

            ProfileAction.OnCancelImport -> {
                setState { copy(pendingImportJson = null) }
            }
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            val success = exportUserData()
            sendEvent(
                ProfileEvent.ShowSnackbar(
                    UiText.Resource(
                        if (success) Res.string.profile_export_success
                        else Res.string.profile_export_failed,
                    ),
                ),
            )
        }
    }

    private fun importData(json: String) {
        viewModelScope.launch {
            when (importUserData(json)) {
                is ImportUserDataUseCase.ImportResult.Success -> {
                    val prefs = userPreferencesRepository
                    setState {
                        copy(
                            isDailyDistichNotificationEnabled = prefs.isDailyDistichNotificationEnabled(),
                            isMemorizationReminderEnabled = prefs.isMemorizationReminderEnabled(),
                            themeMode = prefs.getThemeMode(),
                            fontSizeScale = prefs.getFontSizeScale(),
                        )
                    }
                    sendEvent(
                        ProfileEvent.ShowSnackbar(UiText.Resource(Res.string.profile_import_success)),
                    )
                }
                is ImportUserDataUseCase.ImportResult.Error -> {
                    sendEvent(
                        ProfileEvent.ShowSnackbar(UiText.Resource(Res.string.profile_import_failed)),
                    )
                }
            }
        }
    }

    private fun observeProfileData() {
        viewModelScope.launch {
            combine(
                memorizationRepository.observeActiveSummary(),
                memorizationRepository.observePracticeStreak(),
                userPreferencesRepository.observeGameStats(),
                userPreferencesRepository.observeThemeMode(),
            ) { summary, practiceStreak, gameStats, themeMode ->
                ProfileSnapshot(
                    summary = summary,
                    practiceStreak = practiceStreak,
                    gameStats = gameStats,
                    themeMode = themeMode,
                )
            }.collect { snapshot ->
                refreshFromSnapshot(snapshot)
            }
        }
    }

    private suspend fun refreshFromSnapshot(snapshot: ProfileSnapshot) {
        val statsResult = buildProfileStats(snapshot.gameStats)
        setState {
            copy(
                screenState = UiScreenState.Success,
                themeMode = snapshot.themeMode,
                isDailyDistichNotificationEnabled =
                    userPreferencesRepository.isDailyDistichNotificationEnabled(),
                isMemorizationReminderEnabled =
                    userPreferencesRepository.isMemorizationReminderEnabled(),
                levelProgress = statsResult.levelProgress,
                memorizationStats = statsResult.memorizationStats,
                gameStats = snapshot.gameStats,
                reviewedVersesCount = statsResult.reviewedVersesCount,
                hasCompletedGhazal = statsResult.hasCompletedGhazal,
                previewBadges = statsResult.previewBadges,
                allBadges = statsResult.allBadges,
                allLevels = statsResult.allLevels,
            )
        }
    }

    private fun setMemorizationReminder(enabled: Boolean) {
        userPreferencesRepository.setMemorizationReminderEnabled(enabled)
        setState { copy(isMemorizationReminderEnabled = enabled) }
        viewModelScope.launch {
            memorizationReviewNotificationCoordinator.sync()
        }
    }

    private fun enableDailyDistichNotifications() {
        setState {
            copy(isDailyDistichNotificationEnabled = true)
        }

        if (notificationPermissionGateway.areNotificationsEnabled()) {
            userPreferencesRepository.setDailyDistichNotificationEnabled(true)
            dailyDistichNotificationScheduler.enable(showImmediately = true)
        } else {
            viewModelScope.launch {
                sendEvent(
                    ProfileEvent.RequestNotificationPermission(
                        NotificationPermissionTarget.DailyDistich,
                    ),
                )
            }
        }
    }

    private fun handleNotificationPermissionResult(
        granted: Boolean,
        target: NotificationPermissionTarget,
    ) {
        when (target) {
            NotificationPermissionTarget.DailyDistich ->
                if (granted) {
                    userPreferencesRepository.setDailyDistichNotificationEnabled(true)
                    dailyDistichNotificationScheduler.enable(showImmediately = true)
                    setState {
                        copy(isDailyDistichNotificationEnabled = true)
                    }
                } else {
                    userPreferencesRepository.setDailyDistichNotificationEnabled(false)
                    dailyDistichNotificationScheduler.disable()
                    setState {
                        copy(isDailyDistichNotificationEnabled = false)
                    }
                }

            NotificationPermissionTarget.Remote -> {
                setState {
                    copy(isRemoteNotificationGranted = granted)
                }
            }
        }
    }

    private fun enableRemoteNotifications() {
        if (notificationPermissionGateway.areNotificationsEnabled()) {
            return
        }

        if (userPreferencesRepository.getNotificationPermissionDeclineCount() >=
            MAX_NOTIFICATION_PERMISSION_DECLINES
        ) {
            viewModelScope.launch {
                sendEvent(ProfileEvent.OpenAppNotificationSettings)
            }
        } else {
            viewModelScope.launch {
                sendEvent(
                    ProfileEvent.RequestNotificationPermission(
                        NotificationPermissionTarget.Remote,
                    ),
                )
            }
        }
    }

    private fun disableDailyDistichNotifications() {
        userPreferencesRepository.setDailyDistichNotificationEnabled(false)
        dailyDistichNotificationScheduler.disable()
        setState {
            copy(isDailyDistichNotificationEnabled = false)
        }
    }

    private data class ProfileSnapshot(
        val summary: abkabk.azbarkon.domain.model.memorization.MemorizationSummary,
        val practiceStreak: Int,
        val gameStats: abkabk.azbarkon.domain.model.profile.GameProfileStats,
        val themeMode: ThemeMode,
    )
}
