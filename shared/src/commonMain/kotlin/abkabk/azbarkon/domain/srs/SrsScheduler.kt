package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.domain.model.memorization.SrsGrade
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object SrsScheduler {
    private const val DELIVERY_HOUR = 10
    private const val DELIVERY_MINUTE = 0

    // Grade score deltas
    private const val AGAIN_DELTA = -1.20
    private const val HARD_DELTA = -1.15
    private const val GOOD_DELTA = 1.0
    private const val EASY_DELTA = 1.15
    private const val UNSPECIFIED_DELTA = 0.0

    data class ReviewResult(
        val interval: Int,
        val consecutiveEasy: Int,
        val dueDateMillis: Long
    )

    fun getNewScoreFromGradeEnum(currentScore: Double, grade: SrsGrade): Double =
        when (grade) {
            SrsGrade.AGAIN -> currentScore + AGAIN_DELTA
            SrsGrade.HARD -> currentScore + HARD_DELTA
            SrsGrade.GOOD -> currentScore + GOOD_DELTA
            SrsGrade.EASY -> currentScore + EASY_DELTA
            SrsGrade.UNSPECIFIED -> currentScore + UNSPECIFIED_DELTA
        }

    fun calculatePoemInterval(
        minTotalScore: Double,
        userTotalScore: Double,
        consecutiveEasy: Int,
        clock: Clock = Clock.System,
    ): ReviewResult {

        var interval: Int = 0
        var newConsecutiveEasy: Int = 0

        when {
            userTotalScore > minTotalScore -> {
                newConsecutiveEasy = consecutiveEasy + 1
                interval = newConsecutiveEasy
            }
            userTotalScore == minTotalScore -> {
                newConsecutiveEasy = 0
                interval = 2
            }

            userTotalScore < minTotalScore -> {
                newConsecutiveEasy = 0
                interval = 1
            }
        }

        return ReviewResult(
            interval = interval,
            consecutiveEasy = newConsecutiveEasy,
            dueDateMillis = nextDeliveryMillis(interval = interval, clock = clock)
        )
    }

    private fun nextDeliveryMillis(interval: Int, clock: Clock): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val tomorrow = clock.now()
            .toLocalDateTime(timeZone)
            .date
            .plus(DatePeriod(days = interval))

        return LocalDateTime(
            date = tomorrow,
            time = LocalTime(DELIVERY_HOUR, DELIVERY_MINUTE)
        ).toInstant(timeZone).toEpochMilliseconds()
    }
}
