package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.core.util.localTimezoneOffsetMillis
import abkabk.azbarkon.domain.model.memorization.SrsGrade

object SrsScheduler {
    const val MILLIS_PER_DAY = 86_400_000L
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
        val score: Double,
        val dueDateMillis: Long,
        val consecutiveEasy: Int,
    )

    fun updateVerseScore(currentScore: Double, grade: SrsGrade): Double =
        when (grade) {
            SrsGrade.AGAIN -> currentScore + AGAIN_DELTA
            SrsGrade.HARD -> currentScore + HARD_DELTA
            SrsGrade.GOOD -> currentScore + GOOD_DELTA
            SrsGrade.EASY -> currentScore + EASY_DELTA
            SrsGrade.UNSPECIFIED -> currentScore + UNSPECIFIED_DELTA
        }

    fun calculatePoemInterval(
        verseScores: List<Double>,
        consecutiveEasy: Int,
    ): ReviewResult {
        val total = verseScores.sum()
        val minTotal = verseScores.size * 1.0

        val interval: Int
        val newConsecutiveEasy: Int

        when {
            total < minTotal -> {
                interval = 1
                newConsecutiveEasy = 0
            }
            total == minTotal -> {
                interval = 2
                newConsecutiveEasy = 0
            }
            else -> {
                newConsecutiveEasy = consecutiveEasy + 1
                interval = newConsecutiveEasy
            }
        }

        return ReviewResult(
            interval = interval,
            score = verseScores.average(),
            dueDateMillis = nextDeliveryMillis() + interval * MILLIS_PER_DAY,
            consecutiveEasy = newConsecutiveEasy,
        )
    }

    private fun nextDeliveryMillis(): Long {
        val now = currentTimeMillis()
        val offset = localTimezoneOffsetMillis()
        val localNow = now + offset
        val midnightUtc = localNow - (localNow % MILLIS_PER_DAY)
        val deliveryUtc = midnightUtc + DELIVERY_HOUR * 3_600_000L + DELIVERY_MINUTE * 60_000L
        val nextDeliveryUtc = if (deliveryUtc <= localNow) {
            deliveryUtc + MILLIS_PER_DAY
        } else {
            deliveryUtc
        }
        return nextDeliveryUtc - offset
    }
}
