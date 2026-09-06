package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.platform.KeyValueStore
import abkabk.azbarkon.core.util.dayKeyFromMillis
import abkabk.azbarkon.core.util.nextVisitStreak
import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.model.profile.GameProfileStats
import abkabk.azbarkon.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class LocalUserPreferencesRepository(
    private val keyValueStore: KeyValueStore,
) : UserPreferencesRepository {
    private val gameStatsRefresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val themeModeRefresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val fontSizeScaleRefresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun isDailyDistichNotificationEnabled(): Boolean =
        keyValueStore.getBoolean(KEY_DAILY_DISTICH_NOTIFICATIONS_ENABLED)

    override fun setDailyDistichNotificationEnabled(enabled: Boolean) {
        keyValueStore.putBoolean(KEY_DAILY_DISTICH_NOTIFICATIONS_ENABLED, enabled)
    }

    override fun isMemorizationReminderEnabled(): Boolean =
        keyValueStore.getBoolean(KEY_MEMORIZATION_REMINDER_ENABLED, default = true)

    override fun setMemorizationReminderEnabled(enabled: Boolean) {
        keyValueStore.putBoolean(KEY_MEMORIZATION_REMINDER_ENABLED, enabled)
    }

    override fun getNotificationPermissionDeclineCount(): Int =
        keyValueStore.getInt(KEY_NOTIFICATION_PERMISSION_DECLINE_COUNT)

    override fun incrementNotificationPermissionDeclineCount() {
        keyValueStore.putInt(
            KEY_NOTIFICATION_PERMISSION_DECLINE_COUNT,
            keyValueStore.getInt(KEY_NOTIFICATION_PERMISSION_DECLINE_COUNT) + 1,
        )
    }

    override fun getThemeMode(): ThemeMode = readThemeMode()

    override fun setThemeMode(mode: ThemeMode) {
        keyValueStore.putInt(KEY_THEME_MODE, mode.ordinal)
        themeModeRefresh.tryEmit(Unit)
    }

    override fun observeThemeMode(): Flow<ThemeMode> =
        themeModeRefresh
            .onStart { emit(Unit) }
            .map { readThemeMode() }

    override fun observeFontSizeScale(): Flow<Float> =
        fontSizeScaleRefresh
            .onStart { emit(Unit) }
            .map { readFontSizeScale() }

    override fun getFontSizeScale(): Float = readFontSizeScale()

    override fun setFontSizeScale(scale: Float) {
        val index = FONT_SIZE_SCALES.indexOfFirst { it == scale }.coerceAtLeast(0)
        keyValueStore.putInt(KEY_FONT_SIZE_SCALE, index)
        fontSizeScaleRefresh.tryEmit(Unit)
    }

    override suspend fun getCoinBalance(): Int =
        withContext(Dispatchers.Default) {
            keyValueStore.getInt(KEY_COIN_BALANCE, default = 0)
        }

    override fun adjustCoinBalance(delta: Int): Int {
        val updated = (keyValueStore.getInt(KEY_COIN_BALANCE, default = 0) + delta).coerceAtLeast(0)
        keyValueStore.putInt(KEY_COIN_BALANCE, updated)
        keyValueStore.putBoolean(KEY_COIN_INITIALIZED, true)
        gameStatsRefresh.tryEmit(Unit)
        return updated
    }

    override fun observeGameStats(): Flow<GameProfileStats> =
        gameStatsRefresh
            .onStart { emit(Unit) }
            .map { loadGameStats() }

    override fun recordCompletedSession(
        correct: Int,
        wrong: Int,
        playedAtMillis: Long,
        isPerfect: Boolean,
    ) {
        val playDayKey = dayKeyFromMillis(playedAtMillis)
        val lastPlayDayKey =
            keyValueStore
                .getInt(KEY_GAME_LAST_PLAY_DAY, default = INVALID_DAY_KEY)
                .takeIf { it != INVALID_DAY_KEY }

        val updatedStreak =
            nextVisitStreak(
                currentStreak = keyValueStore.getInt(KEY_GAME_VISIT_STREAK, default = 0),
                lastPlayDayKey = lastPlayDayKey,
                playDayKey = playDayKey,
            )

        keyValueStore.putInt(KEY_GAME_TOTAL_CORRECT, keyValueStore.getInt(KEY_GAME_TOTAL_CORRECT) + correct)
        keyValueStore.putInt(KEY_GAME_TOTAL_WRONG, keyValueStore.getInt(KEY_GAME_TOTAL_WRONG) + wrong)
        keyValueStore.putInt(KEY_GAME_VISIT_STREAK, updatedStreak)
        keyValueStore.putInt(KEY_GAME_LAST_PLAY_DAY, playDayKey)
        keyValueStore.putInt(
            KEY_GAME_COMPLETED_SESSIONS,
            keyValueStore.getInt(KEY_GAME_COMPLETED_SESSIONS) + 1,
        )
        if (isPerfect) {
            keyValueStore.putInt(
                KEY_GAME_PERFECT_SESSIONS,
                keyValueStore.getInt(KEY_GAME_PERFECT_SESSIONS) + 1,
            )
        }
        gameStatsRefresh.tryEmit(Unit)
    }

    private fun loadGameStats(): GameProfileStats =
        GameProfileStats(
            visitStreak = keyValueStore.getInt(KEY_GAME_VISIT_STREAK, default = 0),
            totalCorrectAnswers = keyValueStore.getInt(KEY_GAME_TOTAL_CORRECT, default = 0),
            totalWrongAnswers = keyValueStore.getInt(KEY_GAME_TOTAL_WRONG, default = 0),
            coinBalance = keyValueStore.getInt(KEY_COIN_BALANCE, default = 0),
            completedSessions = keyValueStore.getInt(KEY_GAME_COMPLETED_SESSIONS, default = 0),
            perfectGameSessions = keyValueStore.getInt(KEY_GAME_PERFECT_SESSIONS, default = 0),
        )

    private fun readThemeMode(): ThemeMode =
        ThemeMode.entries.getOrElse(keyValueStore.getInt(KEY_THEME_MODE, default = 0)) {
            ThemeMode.System
        }

    private fun readFontSizeScale(): Float =
        FONT_SIZE_SCALES.getOrElse(keyValueStore.getInt(KEY_FONT_SIZE_SCALE, default = 0)) {
            FONT_SIZE_SCALES.first()
        }

    internal companion object {
        val FONT_SIZE_SCALES = floatArrayOf(1f, 1.1f, 1.2f)
        const val KEY_FONT_SIZE_SCALE = "font_size_scale"
        const val KEY_DAILY_DISTICH_NOTIFICATIONS_ENABLED = "daily_distich_notifications_enabled"
        const val KEY_MEMORIZATION_REMINDER_ENABLED = "memorization_reminder_enabled"
        const val KEY_NOTIFICATION_PERMISSION_DECLINE_COUNT = "notification_permission_decline_count"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_COIN_BALANCE = "game_coin_balance"
        const val KEY_COIN_INITIALIZED = "game_coin_initialized"
        const val KEY_GAME_VISIT_STREAK = "game_visit_streak"
        const val KEY_GAME_LAST_PLAY_DAY = "game_last_play_day"
        const val KEY_GAME_TOTAL_CORRECT = "game_total_correct"
        const val KEY_GAME_TOTAL_WRONG = "game_total_wrong"
        const val KEY_GAME_COMPLETED_SESSIONS = "game_completed_sessions"
        const val KEY_GAME_PERFECT_SESSIONS = "game_perfect_sessions"
        const val INVALID_DAY_KEY = -1
    }
}
