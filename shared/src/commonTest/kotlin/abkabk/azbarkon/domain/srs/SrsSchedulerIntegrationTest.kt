package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredPoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import abkabk.azbarkon.domain.model.profile.BadgeCatalog
import abkabk.azbarkon.testing.FakeMemorizationReviewNotificationScheduler
import abkabk.azbarkon.testing.FakeUserPreferencesRepository
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test

class SrsSchedulerIntegrationTest {

    private val baseTime = Instant.parse("2026-01-01T10:00:00Z")
    private var currentTime = baseTime
    private val testClock = object : Clock {
        override fun now(): Instant = currentTime
    }

    @Test
    fun `five consecutive easy reviews marks poem as completed`() {
        var consecutiveEasy = 0
        var lastDueDate = 0L

        repeat(5) { i ->
            val result = SrsScheduler.calculatePoemInterval(
                minTotalScore = 2.0,
                userTotalScore = 4.0, // above min → easy path
                consecutiveEasy = consecutiveEasy,
                clock = testClock,
            )

            consecutiveEasy = result.consecutiveEasy
            lastDueDate = result.dueDateMillis

            // each iteration: consecutiveEasy should be i+1
            assertThat(result.consecutiveEasy).isEqualTo(i + 1)
            // interval grows: 1, 2, 3, 4, 5
            assertThat(result.interval).isEqualTo(i + 1)
        }

        // after 5 consecutive easy, threshold is met
        assertThat(consecutiveEasy).isEqualTo(5)
        assertThat(consecutiveEasy).isGreaterThan(4)

        // verify badge would be earned
        val badgeEarned = BadgeCatalog.resolveEarned(
            badgeId = 1,
            hasCompletedGhazal = true,
            reviewedVersesCount = 0,
            gameVisitStreak = 0,
            completedPoemCount = 0,
            perfectGameSessions = 0,
        )
        assertThat(badgeEarned).isTrue()
    }

    @Test
    fun `due date advances with each easy review`() {
        var consecutiveEasy = 0
        var previousDueDate = 0L

        repeat(5) {
            val result = SrsScheduler.calculatePoemInterval(
                minTotalScore = 2.0,
                userTotalScore = 4.0,
                consecutiveEasy = consecutiveEasy,
                clock = testClock,
            )

            // due date should be in the future
            assertThat(result.dueDateMillis).isGreaterThan(currentTime.toEpochMilliseconds())

            // due date should increase with each review
            assertThat(result.dueDateMillis).isGreaterThan(previousDueDate)

            consecutiveEasy = result.consecutiveEasy
            previousDueDate = result.dueDateMillis

            // advance time to the due date for next iteration
            currentTime = Instant.fromEpochMilliseconds(
                result.dueDateMillis
            )
        }
    }

    @Test
    fun `resetting to again breaks consecutive easy streak`() {
        // build up 3 consecutive easy
        var consecutiveEasy = 0
        repeat(3) {
            val result = SrsScheduler.calculatePoemInterval(
                minTotalScore = 2.0,
                userTotalScore = 4.0,
                consecutiveEasy = consecutiveEasy,
                clock = testClock,
            )
            consecutiveEasy = result.consecutiveEasy
            currentTime = Instant.fromEpochMilliseconds(result.dueDateMillis)
        }
        assertThat(consecutiveEasy).isEqualTo(3)

        // one again grade resets streak
        val resetResult = SrsScheduler.calculatePoemInterval(
            minTotalScore = 2.0,
            userTotalScore = -0.5, // below min → again path
            consecutiveEasy = consecutiveEasy,
            clock = testClock,
        )
        assertThat(resetResult.consecutiveEasy).isEqualTo(0)
        assertThat(resetResult.interval).isEqualTo(1)
    }

    @Test
    fun `hard grade does not increment consecutive easy`() {
        val result = SrsScheduler.calculatePoemInterval(
            minTotalScore = 2.0,
            userTotalScore = 2.0, // equals min → hard path
            consecutiveEasy = 3,
            clock = testClock,
        )
        assertThat(result.consecutiveEasy).isEqualTo(0)
        assertThat(result.interval).isEqualTo(2)
    }

    @Test
    fun `full scenario add poem then 5 easy sessions then completed`() {
        // simulate the full flow:
        // 1. poem added, interval=1, dueDate=now (due immediately)
        // 2. first review: easy → consecutiveEasy=1, interval=1
        // 3. second review: easy → consecutiveEasy=2, interval=2
        // 4. third review: easy → consecutiveEasy=3, interval=3
        // 5. fourth review: easy → consecutiveEasy=4, interval=4
        // 6. fifth review: easy → consecutiveEasy=5 → COMPLETED

        var consecutiveEasy = 0
        val reviews = mutableListOf<Pair<Int, Long>>() // interval, dueDate

        repeat(5) { i ->
            val result = SrsScheduler.calculatePoemInterval(
                minTotalScore = 2.0,
                userTotalScore = 4.0,
                consecutiveEasy = consecutiveEasy,
                clock = testClock,
            )
            reviews.add(result.interval to result.dueDateMillis)
            consecutiveEasy = result.consecutiveEasy
            currentTime = Instant.fromEpochMilliseconds(result.dueDateMillis)
        }

        // verify progression
        assertThat(reviews.map { it.first }).isEqualTo(listOf(1, 2, 3, 4, 5))
        assertThat(reviews.map { it.second }).isEqualTo(
            reviews.map { it.second }.sorted()
        )

        // verify completion threshold
        val isCompleted = consecutiveEasy >= 5
        assertThat(isCompleted).isTrue()

        // verify badge
        val badgeEarned = BadgeCatalog.resolveEarned(
            badgeId = 1,
            hasCompletedGhazal = isCompleted,
            reviewedVersesCount = 0,
            gameVisitStreak = 0,
            completedPoemCount = 0,
            perfectGameSessions = 0,
        )
        assertThat(badgeEarned).isTrue()
    }

    @Test
    fun `notification fires after adding poem and sync`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val localDataSource = MutableFakeMemorizationLocalDataSource()
        val coordinator = MemorizationReviewNotificationCoordinator(
            localDataSource = localDataSource,
            scheduler = scheduler,
            userPreferencesRepository = preferences,
        )

        // no active poems → disabled
        coordinator.sync()
        assertThat(scheduler.isEnabled).isFalse()
        assertThat(scheduler.disableCallCount).isEqualTo(1)

        // add a poem
        localDataSource.activePoemCount = 1
        coordinator.sync()
        assertThat(scheduler.isEnabled).isTrue()
        assertThat(scheduler.enableCallCount).isEqualTo(1)
        assertThat(scheduler.lastDeliveryHour).isEqualTo(10)
        assertThat(scheduler.lastDeliveryMinute).isEqualTo(0)
    }

    @Test
    fun `notification disables when reminder turned off even with active poems`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val localDataSource = MutableFakeMemorizationLocalDataSource(activePoemCount = 3)
        val coordinator = MemorizationReviewNotificationCoordinator(
            localDataSource = localDataSource,
            scheduler = scheduler,
            userPreferencesRepository = preferences,
        )

        // active poems + reminder on → enabled
        coordinator.sync()
        assertThat(scheduler.isEnabled).isTrue()

        // turn off reminder
        preferences.setMemorizationReminderEnabled(false)
        coordinator.sync()
        assertThat(scheduler.isEnabled).isFalse()
        assertThat(scheduler.disableCallCount).isEqualTo(1)
    }

    @Test
    fun `notification disables after removing all poems`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val localDataSource = MutableFakeMemorizationLocalDataSource(activePoemCount = 2)
        val coordinator = MemorizationReviewNotificationCoordinator(
            localDataSource = localDataSource,
            scheduler = scheduler,
            userPreferencesRepository = preferences,
        )

        coordinator.sync()
        assertThat(scheduler.isEnabled).isTrue()

        // remove all poems
        localDataSource.activePoemCount = 0
        coordinator.sync()
        assertThat(scheduler.isEnabled).isFalse()
        assertThat(scheduler.disableCallCount).isEqualTo(1)
    }

    @Test
    fun `srs schedule then notification sync full lifecycle`() = runTest {
        val preferences = FakeUserPreferencesRepository()
        val scheduler = FakeMemorizationReviewNotificationScheduler()
        val localDataSource = MutableFakeMemorizationLocalDataSource()
        val coordinator = MemorizationReviewNotificationCoordinator(
            localDataSource = localDataSource,
            scheduler = scheduler,
            userPreferencesRepository = preferences,
        )

        // 1. no poems → disabled
        coordinator.sync()
        assertThat(scheduler.disableCallCount).isEqualTo(1)

        // 2. add poem via SRS, time travel through 5 reviews
        localDataSource.activePoemCount = 1
        var consecutiveEasy = 0
        repeat(5) {
            val result = SrsScheduler.calculatePoemInterval(
                minTotalScore = 2.0,
                userTotalScore = 4.0,
                consecutiveEasy = consecutiveEasy,
                clock = testClock,
            )
            consecutiveEasy = result.consecutiveEasy
            currentTime = Instant.fromEpochMilliseconds(result.dueDateMillis)
        }

        // 3. sync → notifications enabled (poems still active)
        coordinator.sync()
        assertThat(scheduler.isEnabled).isTrue()
        assertThat(scheduler.enableCallCount).isEqualTo(1)

        // 4. simulate poem completed → remove from active
        localDataSource.activePoemCount = 0
        coordinator.sync()
        assertThat(scheduler.disableCallCount).isEqualTo(2)
    }

    private class MutableFakeMemorizationLocalDataSource(
        var activePoemCount: Int = 0,
    ) : MemorizationLocalDataSource {
        override suspend fun countActivePoems(): Int = activePoemCount
        override suspend fun isPoemActive(poemId: Int): Boolean = activePoemCount > 0
        override suspend fun insertPoem(poemId: Int, addedAtMillis: Long, status: String, interval: Int, dueDateMillis: Long, consecutiveCorrect: Int, totalCard: Int) = Unit
        override suspend fun deletePoem(poemId: Int) = Unit
        override suspend fun getPoemIdsByStatus(status: String): List<Int> = emptyList()
        override suspend fun getPoemAddedAt(poemId: Int): Long? = null
        override suspend fun getMemorizationPoem(poemId: Int): StoredPoem? = null
        override suspend fun insertCards(cards: List<SrsCard>) = Unit
        override suspend fun getCardById(cardId: Long): SrsCard? = null
        override suspend fun getDueCards(poemId: Int): List<SrsCard> = emptyList()
        override suspend fun getCardsByPoemId(poemId: Int): List<SrsCard> = emptyList()
        override suspend fun countDueCards(poemId: Int): Int = 0
        override suspend fun updatePoemSchedule(poemId: Int, status: String, interval: Int, dueDateMillis: Long, consecutiveCorrect: Int) = Unit
        override suspend fun countCardsByPoemId(poemId: Int): Int = 0
        override suspend fun getLastReviewLogByPoemId(poemId: Int): StoredReviewLog = StoredReviewLog(id = -1, poemId = poemId, reviewRound = 0, minTotalScore = 0.0, userTotalScore = 0.0, cardIndex = 0, sessionReviewed = 0, sessionMistakes = 0, sessionLearned = 0)
        override suspend fun insertReviewLog(poemId: Int, reviewRound: Int, minTotalScore: Double, userTotalScore: Double, cardIndex: Int, sessionReviewed: Int, sessionMistakes: Int, sessionLearned: Int) = Unit
        override suspend fun countReviewedVerses(): Int = 0
        override suspend fun dumpActivePoems(): List<StoredPoem> = emptyList()
        override suspend fun dumpCards(): List<SrsCard> = emptyList()
        override suspend fun dumpReviewLogs(): List<StoredReviewLog> = emptyList()
        override suspend fun replaceAll(activePoems: List<StoredPoem>, cards: List<SrsCard>, reviewLogs: List<StoredReviewLog>) = Unit
        override suspend fun findPoetIdByName(nameFragment: String): Result<Int, DataError.Local> = Result.Error(DataError.Local.UNKNOWN)
        override suspend fun findCategoryByPoetAndText(poetId: Int, textFragment: String): Result<Pair<Int, String>, DataError.Local> = Result.Error(DataError.Local.UNKNOWN)
    }
}
