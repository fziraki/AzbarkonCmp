package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.profile.BadgeCatalog
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
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
}
