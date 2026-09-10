package abkabk.azbarkon.domain.usecase

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.data.backup.UserBackupManager
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.platform.DailyDistichNotificationScheduler
import abkabk.azbarkon.domain.repository.UserPreferencesRepository

class ImportUserDataUseCase(
    private val userBackupManager: UserBackupManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dailyDistichNotificationScheduler: DailyDistichNotificationScheduler,
    private val memorizationReviewNotificationCoordinator: MemorizationReviewNotificationCoordinator,
) {
    sealed interface ImportResult {
        data object Success : ImportResult
        data object Error : ImportResult
    }

    suspend operator fun invoke(json: String): ImportResult {
        return when (val result = userBackupManager.importJson(json)) {
            is Result.Success -> {
                val prefs = result.data.prefs
                userPreferencesRepository.setThemeMode(
                    ThemeMode.entries.getOrElse(prefs.themeMode) { ThemeMode.System },
                )
                userPreferencesRepository.setFontSizeScale(prefs.fontSizeScale)
                userPreferencesRepository.adjustCoinBalance(0)
                memorizationReviewNotificationCoordinator.sync()
                if (prefs.dailyDistichNotificationsEnabled) {
                    dailyDistichNotificationScheduler.enable(showImmediately = false)
                } else {
                    dailyDistichNotificationScheduler.disable()
                }
                ImportResult.Success
            }
            is Result.Error -> ImportResult.Error
        }
    }
}
