package abkabk.azbarkon.domain.memorization

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.testing.FakeMemorizationReviewNotificationScheduler
import abkabk.azbarkon.testing.FakeUserPreferencesRepository
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MemorizationReviewNotificationCoordinatorTest {
    @Test
    fun `sync disables scheduler when no active poems`() = runTest {
        val localDataSource = FakeMemorizationLocalDataSource(activePoemCount = 0)
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val preferences = FakeUserPreferencesRepository()
        val coordinator =
            MemorizationReviewNotificationCoordinator(
                localDataSource = localDataSource,
                scheduler = scheduler,
                userPreferencesRepository = preferences,
            )

        coordinator.sync()

        assertThat(scheduler.disableCallCount).isEqualTo(1)
        assertThat(scheduler.isEnabled).isFalse()
    }

    @Test
    fun `sync enables scheduler when active poems exist`() = runTest {
        val localDataSource = FakeMemorizationLocalDataSource(activePoemCount = 2)
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val preferences = FakeUserPreferencesRepository()
        val coordinator =
            MemorizationReviewNotificationCoordinator(
                localDataSource = localDataSource,
                scheduler = scheduler,
                userPreferencesRepository = preferences,
            )

        coordinator.sync()

        assertThat(scheduler.enableCallCount).isEqualTo(1)
        assertThat(scheduler.isEnabled).isTrue()
        assertThat(scheduler.lastDeliveryHour).isEqualTo(10)
        assertThat(scheduler.lastDeliveryMinute).isEqualTo(0)
    }

    @Test
    fun `sync disables scheduler when reminder preference is off`() = runTest {
        val localDataSource = FakeMemorizationLocalDataSource(activePoemCount = 2)
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val preferences =
            FakeUserPreferencesRepository().apply {
                setMemorizationReminderEnabled(false)
            }
        val coordinator =
            MemorizationReviewNotificationCoordinator(
                localDataSource = localDataSource,
                scheduler = scheduler,
                userPreferencesRepository = preferences,
            )

        coordinator.sync()

        assertThat(scheduler.disableCallCount).isEqualTo(1)
        assertThat(scheduler.isEnabled).isFalse()
    }

    private class FakeMemorizationLocalDataSource(
        private val activePoemCount: Int,
    ) : MemorizationLocalDataSource {
        override suspend fun countActivePoems(): Int = activePoemCount

        override suspend fun isPoemActive(poemId: Int): Boolean = false

        override suspend fun insertPoem(
            poemId: Int,
            addedAtMillis: Long,
            status: String,
            interval: Int,
            dueDateMillis: Long,
            consecutiveCorrect: Int,
            totalCard: Int,
        ) = Unit

        override suspend fun deletePoem(poemId: Int) = Unit

        override suspend fun getPoemIdsByStatus(status: String): List<Int> = emptyList()

        override suspend fun getPoemAddedAt(poemId: Int): Long? = null

        override suspend fun getMemorizationPoem(poemId: Int): abkabk.azbarkon.domain.model.memorization.StoredPoem? = null

        override suspend fun insertCards(cards: List<SrsCard>) = Unit

        override suspend fun getCardById(cardId: Long): SrsCard? = null

        override suspend fun getDueCards(poemId: Int): List<SrsCard> = emptyList()

        override suspend fun getCardsByPoemId(poemId: Int): List<SrsCard> = emptyList()

        override suspend fun countDueCards(poemId: Int): Int = 0

        override suspend fun updatePoemSchedule(
            poemId: Int,
            status: String,
            interval: Int,
            dueDateMillis: Long,
            consecutiveCorrect: Int,
        ) = Unit

        override suspend fun countCardsByPoemId(poemId: Int): Int = 0

        override suspend fun getLastReviewLogByPoemId(poemId: Int): abkabk.azbarkon.domain.model.memorization.StoredReviewLog =
            abkabk.azbarkon.domain.model.memorization.StoredReviewLog(
                id = -1, poemId = poemId, reviewRound = 0, minTotalScore = 0.0,
                userTotalScore = 0.0, cardIndex = 0, sessionReviewed = 0,
                sessionMistakes = 0, sessionLearned = 0,
            )

        override suspend fun insertReviewLog(
            poemId: Int,
            reviewRound: Int,
            minTotalScore: Double,
            userTotalScore: Double,
            cardIndex: Int,
            sessionReviewed: Int,
            sessionMistakes: Int,
            sessionLearned: Int,
        ) = Unit

        override suspend fun getReviewDayKeys(): List<Int> = emptyList()

        override suspend fun countReviewedVerses(): Int = 0

        override suspend fun dumpActivePoems(): List<abkabk.azbarkon.domain.model.memorization.StoredPoem> =
            emptyList()

        override suspend fun dumpCards(): List<SrsCard> = emptyList()

        override suspend fun dumpReviewLogs(): List<abkabk.azbarkon.domain.model.memorization.StoredReviewLog> =
            emptyList()

        override suspend fun replaceAll(
            activePoems: List<abkabk.azbarkon.domain.model.memorization.StoredPoem>,
            cards: List<SrsCard>,
            reviewLogs: List<abkabk.azbarkon.domain.model.memorization.StoredReviewLog>,
        ) = Unit

        override suspend fun findPoetIdByName(nameFragment: String): Result<Int, DataError.Local> =
            Result.Error(DataError.Local.UNKNOWN)

        override suspend fun findCategoryByPoetAndText(
            poetId: Int,
            textFragment: String,
        ): Result<Pair<Int, String>, DataError.Local> = Result.Error(DataError.Local.UNKNOWN)
    }
}
