package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.domain.model.memorization.SrsGrade
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SrsSchedulerTest {
    @Test
    fun `again decreases score by 1_20`() {
        val score = SrsScheduler.getNewScoreFromGradeEnum(0.0, SrsGrade.AGAIN)
        assertThat(score).isEqualTo(-1.20)
    }

    @Test
    fun `hard decreases score by 1_15`() {
        val score = SrsScheduler.getNewScoreFromGradeEnum(0.0, SrsGrade.HARD)
        assertThat(score).isEqualTo(-1.15)
    }

    @Test
    fun `good increases score by 1`() {
        val score = SrsScheduler.getNewScoreFromGradeEnum(0.0, SrsGrade.GOOD)
        assertThat(score).isEqualTo(1.0)
    }

    @Test
    fun `easy increases score by 1_15`() {
        val score = SrsScheduler.getNewScoreFromGradeEnum(0.0, SrsGrade.EASY)
        assertThat(score).isEqualTo(1.15)
    }

    @Test
    fun `unspecified keeps score unchanged`() {
        val score = SrsScheduler.getNewScoreFromGradeEnum(0.0, SrsGrade.UNSPECIFIED)
        assertThat(score).isEqualTo(0.0)
    }

    @Test
    fun `poem interval is 1 when total below minTotal`() {
        val result = SrsScheduler.calculatePoemInterval(
            minTotalScore = 2.0,
            userTotalScore = -0.5,
            consecutiveEasy = 0,
        )
        assertThat(result.interval).isEqualTo(1)
        assertThat(result.consecutiveEasy).isEqualTo(0)
    }

    @Test
    fun `poem interval is 2 when total equals minTotal`() {
        val result = SrsScheduler.calculatePoemInterval(
            minTotalScore = 2.0,
            userTotalScore = 2.0,
            consecutiveEasy = 0,
        )
        assertThat(result.interval).isEqualTo(2)
        assertThat(result.consecutiveEasy).isEqualTo(0)
    }

    @Test
    fun `poem interval increments consecutiveEasy when total above minTotal`() {
        val result = SrsScheduler.calculatePoemInterval(
            minTotalScore = 2.0,
            userTotalScore = 4.0,
            consecutiveEasy = 2,
        )
        assertThat(result.interval).isEqualTo(3)
        assertThat(result.consecutiveEasy).isEqualTo(3)
    }

}
