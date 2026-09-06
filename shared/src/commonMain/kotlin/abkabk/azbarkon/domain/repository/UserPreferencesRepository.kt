package abkabk.azbarkon.domain.repository

import abkabk.azbarkon.domain.model.ThemeMode
import abkabk.azbarkon.domain.model.profile.GameProfileStats
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun isDailyDistichNotificationEnabled(): Boolean

    fun setDailyDistichNotificationEnabled(enabled: Boolean)

    fun isMemorizationReminderEnabled(): Boolean

    fun setMemorizationReminderEnabled(enabled: Boolean)

    fun getNotificationPermissionDeclineCount(): Int

    fun incrementNotificationPermissionDeclineCount()

    fun getThemeMode(): ThemeMode

    fun setThemeMode(mode: ThemeMode)

    fun observeThemeMode(): Flow<ThemeMode>

    fun observeFontSizeScale(): Flow<Float>

    fun getFontSizeScale(): Float

    fun setFontSizeScale(scale: Float)

    suspend fun getCoinBalance(): Int

    fun adjustCoinBalance(delta: Int): Int

    fun observeGameStats(): Flow<GameProfileStats>

    fun recordCompletedSession(
        correct: Int,
        wrong: Int,
        playedAtMillis: Long,
        isPerfect: Boolean = false,
    )
}
