package abkabk.azbarkon.core.util

import abkabk.azbarkon.domain.model.games.GameConstants
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StreakDayUtilsTest {
    @Test
    fun `nextVisitStreak increments on consecutive day`() {
        val streak = nextVisitStreak(currentStreak = 2, lastPlayDayKey = 10, playDayKey = 11)

        assertThat(streak).isEqualTo(3)
    }

    @Test
    fun `nextVisitStreak resets after gap`() {
        val streak = nextVisitStreak(currentStreak = 4, lastPlayDayKey = 10, playDayKey = 12)

        assertThat(streak).isEqualTo(1)
    }

    @Test
    fun `recordCompletedSession accumulates totals and streak in fake preferences`() =
        runTest {
            val preferences = abkabk.azbarkon.testing.FakeUserPreferencesRepository()
            val dayMillis = 11L * 86_400_000L

            preferences.recordCompletedSession(correct = 3, wrong = 1, playedAtMillis = dayMillis)
            preferences.recordCompletedSession(correct = 2, wrong = 2, playedAtMillis = dayMillis)

            val stats = preferences.observeGameStats().first()
            assertThat(stats.totalCorrectAnswers).isEqualTo(5)
            assertThat(stats.totalWrongAnswers).isEqualTo(3)
            assertThat(stats.visitStreak).isEqualTo(1)
            assertThat(stats.completedSessions).isEqualTo(2)
            assertThat(stats.perfectGameSessions).isEqualTo(0)
        }

    @Test
    fun `recordCompletedSession increments perfect sessions when flagged`() =
        runTest {
            val preferences = abkabk.azbarkon.testing.FakeUserPreferencesRepository()
            val dayMillis = 11L * 86_400_000L

            preferences.recordCompletedSession(
                correct = GameConstants.QUIZ_COUNT,
                wrong = 0,
                playedAtMillis = dayMillis,
                isPerfect = true,
            )

            val stats = preferences.observeGameStats().first()
            assertThat(stats.perfectGameSessions).isEqualTo(1)
        }
}
