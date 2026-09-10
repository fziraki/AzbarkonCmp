package abkabk.azbarkon.data.backup

import abkabk.azbarkon.core.domain.result.Error
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.platform.KeyValueStore
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.data.repository.LocalSavedPoemRepository
import abkabk.azbarkon.data.repository.LocalUserPreferencesRepository
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.StoredPoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val BACKUP_FORMAT = 1

@Serializable
data class UserBackupFile(
    val format: Int = BACKUP_FORMAT,
    val exportedAtMillis: Long = 0,
    val prefs: UserBackupPrefs = UserBackupPrefs(),
    val memorization: UserBackupMemorization = UserBackupMemorization(),
)

@Serializable
data class UserBackupPrefs(
    val themeMode: Int = 0,
    val coinInitialized: Boolean = false,
    val gameCoinBalance: Int = 0,
    val gameVisitStreak: Int = 0,
    val gameLastPlayDay: Int = 0,
    val gameTotalCorrect: Int = 0,
    val gameTotalWrong: Int = 0,
    val gameCompletedSessions: Int = 0,
    val gamePerfectSessions: Int = 0,
    val likedPoemIds: Set<Int> = emptySet(),
    val bookmarkedPoemIds: Set<Int> = emptySet(),
    val dailyDistichNotificationsEnabled: Boolean = false,
    val memorizationReminderEnabled: Boolean = false,
    val fontSizeScale: Float = 1f,
)

@Serializable
data class UserBackupMemorization(
    val activePoems: List<StoredPoem> = emptyList(),
    val cards: List<SrsCard> = emptyList(),
    val reviewLogs: List<StoredReviewLog> = emptyList(),
)

sealed interface BackupError : Error {
    data object UnsupportedFormat : BackupError
    data object InvalidData : BackupError
}

private val backupJson =
    Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

internal fun encodeBackup(file: UserBackupFile): String = backupJson.encodeToString(UserBackupFile.serializer(), file)

internal fun decodeBackup(jsonString: String): Result<UserBackupFile, BackupError> =
    try {
        val file = backupJson.decodeFromString(UserBackupFile.serializer(), jsonString)
        if (file.format != BACKUP_FORMAT) {
            Result.Error(BackupError.UnsupportedFormat)
        } else {
            Result.Success(file)
        }
    } catch (e: IllegalArgumentException) {
        Napier.e("decodeBackup failed", e)
        Result.Error(BackupError.InvalidData)
    }

interface UserBackupManager {
    suspend fun exportJson(): String

    suspend fun importJson(jsonString: String): Result<UserBackupFile, BackupError>
}

class LocalUserBackupManager(
    private val keyValueStore: KeyValueStore,
    private val memorizationLocalDataSource: MemorizationLocalDataSource,
) : UserBackupManager {
    override suspend fun exportJson(): String =
        encodeBackup(
            UserBackupFile(
                exportedAtMillis = currentTimeMillis(),
                prefs = readPrefs(),
                memorization =
                    UserBackupMemorization(
                        activePoems = memorizationLocalDataSource.dumpActivePoems(),
                        cards = memorizationLocalDataSource.dumpCards(),
                        reviewLogs = memorizationLocalDataSource.dumpReviewLogs(),
                    ),
            ),
        )

    override suspend fun importJson(jsonString: String): Result<UserBackupFile, BackupError> =
        decodeBackup(jsonString).onSuccess { file ->
            writePrefs(file.prefs)
            memorizationLocalDataSource.replaceAll(
                activePoems = file.memorization.activePoems,
                cards = file.memorization.cards,
                reviewLogs = file.memorization.reviewLogs,
            )
        }

    private fun readPrefs(): UserBackupPrefs =
        UserBackupPrefs(
            themeMode = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_THEME_MODE),
            coinInitialized =
                keyValueStore.getBoolean(LocalUserPreferencesRepository.KEY_COIN_INITIALIZED),
            gameCoinBalance = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_COIN_BALANCE),
            gameVisitStreak = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_VISIT_STREAK),
            gameLastPlayDay = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_LAST_PLAY_DAY),
            gameTotalCorrect = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_TOTAL_CORRECT),
            gameTotalWrong = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_TOTAL_WRONG),
            gameCompletedSessions = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_COMPLETED_SESSIONS),
            gamePerfectSessions = keyValueStore.getInt(LocalUserPreferencesRepository.KEY_GAME_PERFECT_SESSIONS),
            likedPoemIds = keyValueStore.getIntSet(LocalSavedPoemRepository.KEY_LIKED),
            bookmarkedPoemIds = keyValueStore.getIntSet(LocalSavedPoemRepository.KEY_BOOKMARKED),
            dailyDistichNotificationsEnabled =
                keyValueStore.getBoolean(LocalUserPreferencesRepository.KEY_DAILY_DISTICH_NOTIFICATIONS_ENABLED),
            memorizationReminderEnabled =
                keyValueStore.getBoolean(LocalUserPreferencesRepository.KEY_MEMORIZATION_REMINDER_ENABLED, default = true),
            fontSizeScale = readFontSizeScale(),
        )

    private fun writePrefs(prefs: UserBackupPrefs) {
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_THEME_MODE, prefs.themeMode)
        keyValueStore.putBoolean(
            LocalUserPreferencesRepository.KEY_COIN_INITIALIZED,
            prefs.coinInitialized,
        )
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_COIN_BALANCE, prefs.gameCoinBalance)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_VISIT_STREAK, prefs.gameVisitStreak)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_LAST_PLAY_DAY, prefs.gameLastPlayDay)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_TOTAL_CORRECT, prefs.gameTotalCorrect)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_TOTAL_WRONG, prefs.gameTotalWrong)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_COMPLETED_SESSIONS, prefs.gameCompletedSessions)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_GAME_PERFECT_SESSIONS, prefs.gamePerfectSessions)
        keyValueStore.putIntSet(LocalSavedPoemRepository.KEY_LIKED, prefs.likedPoemIds)
        keyValueStore.putIntSet(LocalSavedPoemRepository.KEY_BOOKMARKED, prefs.bookmarkedPoemIds)
        keyValueStore.putBoolean(
            LocalUserPreferencesRepository.KEY_DAILY_DISTICH_NOTIFICATIONS_ENABLED,
            prefs.dailyDistichNotificationsEnabled,
        )
        keyValueStore.putBoolean(
            LocalUserPreferencesRepository.KEY_MEMORIZATION_REMINDER_ENABLED,
            prefs.memorizationReminderEnabled,
        )
        writeFontSizeScale(prefs.fontSizeScale)
    }

    private fun readFontSizeScale(): Float =
        FONT_SIZE_SCALES.getOrElse(keyValueStore.getInt(LocalUserPreferencesRepository.KEY_FONT_SIZE_SCALE, default = 0)) {
            FONT_SIZE_SCALES.first()
        }

    private fun writeFontSizeScale(scale: Float) {
        val index = FONT_SIZE_SCALES.indexOfFirst { it == scale }.coerceAtLeast(0)
        keyValueStore.putInt(LocalUserPreferencesRepository.KEY_FONT_SIZE_SCALE, index)
    }

    companion object {
        private val FONT_SIZE_SCALES = floatArrayOf(1f, 1.1f, 1.2f)
    }
}
