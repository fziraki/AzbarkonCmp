package abkabk.azbarkon.testing

import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.data.backup.BackupError
import abkabk.azbarkon.data.backup.UserBackupFile
import abkabk.azbarkon.data.backup.UserBackupManager
import abkabk.azbarkon.data.backup.encodeBackup

class FakeUserBackupManager(
    private val failImport: Boolean = false,
    private val preferences: FakeUserPreferencesRepository? = null,
) : UserBackupManager {
    var importCallCount: Int = 0
    var lastImportedJson: String? = null

    override suspend fun exportJson(): String = encodeBackup(UserBackupFile())

    override suspend fun importJson(jsonString: String): Result<UserBackupFile, BackupError> {
        importCallCount += 1
        lastImportedJson = jsonString
        return if (failImport) {
            Result.Error(BackupError.InvalidData)
        } else {
            val prefs =
                UserBackupFile().prefs.copy(
                    dailyDistichNotificationsEnabled = true,
                    memorizationReminderEnabled = true,
                )
            preferences?.apply {
                setDailyDistichNotificationEnabled(prefs.dailyDistichNotificationsEnabled)
                setMemorizationReminderEnabled(prefs.memorizationReminderEnabled)
            }
            Result.Success(UserBackupFile(prefs = prefs))
        }
    }
}
