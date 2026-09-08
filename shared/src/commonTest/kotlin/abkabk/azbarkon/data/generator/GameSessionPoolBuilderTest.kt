package abkabk.azbarkon.data.generator

import abkabk.azbarkon.domain.model.games.GameType
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class GameSessionPoolBuilderTest {
    @Test
    fun `extracts distichs from paired verses`() {
        val verses =
            listOf(
                VerseRow(vorder = 1, position = 0, text = "a0"),
                VerseRow(vorder = 2, position = 1, text = "a1"),
                VerseRow(vorder = 3, position = 0, text = "b0"),
                VerseRow(vorder = 4, position = 1, text = "b1"),
                VerseRow(vorder = 5, position = 0, text = "c0"),
                VerseRow(vorder = 6, position = 1, text = "c1"),
            )

        val extraction =
            GameSessionPoolBuilder.extractFromVerses(
                poemId = 10,
                poetId = 2,
                poetName = "حافظ",
                verses = verses,
            )

        assertThat(extraction.distichs).hasSize(3)
        assertThat(extraction.distichs.first().firstHemistich).isEqualTo("a0")
        assertThat(extraction.distichs.first().secondHemistich).isEqualTo("a1")
    }

    @Test
    fun `rejects poems below minimum distich count`() {
        val verses =
            listOf(
                VerseRow(vorder = 1, position = 0, text = "a0"),
                VerseRow(vorder = 2, position = 1, text = "a1"),
            )

        val extraction =
            GameSessionPoolBuilder.extractFromVerses(
                poemId = 10,
                poetId = 2,
                poetName = "حافظ",
                verses = verses,
            )

        assertThat(
            GameSessionPoolBuilder.isPoemAcceptable(
                gameType = GameType.NEXT_VERSE,
                distichCount = extraction.distichs.size,
                organizeCount = extraction.organizeWindows.size,
                totalVerses = verses.groupBy { it.vorder }.size,
            ),
        ).isFalse()
    }

    @Test
    fun `accepts organize poem when windows exist`() {
        assertThat(
            GameSessionPoolBuilder.isPoemAcceptable(
                gameType = GameType.ORGANIZE_POEM,
                distichCount = 0,
                organizeCount = 1,
                totalVerses = 0,
            ),
        ).isTrue()
    }

    @Test
    fun `extracts organize windows from two consecutive distichs`() {
        val verses =
            listOf(
                VerseRow(vorder = 1, position = 0, text = "a0"),
                VerseRow(vorder = 2, position = 1, text = "a1"),
                VerseRow(vorder = 3, position = 0, text = "b0"),
                VerseRow(vorder = 4, position = 1, text = "b1"),
                VerseRow(vorder = 5, position = 0, text = "c0"),
                VerseRow(vorder = 6, position = 1, text = "c1"),
            )

        val extraction =
            GameSessionPoolBuilder.extractFromVerses(
                poemId = 11,
                poetId = 2,
                poetName = "حافظ",
                verses = verses,
            )

        assertThat(extraction.organizeWindows).hasSize(2)
        assertThat(extraction.organizeWindows.first().lines)
            .isEqualTo(listOf("a0", "a1", "b0", "b1"))
        assertThat(extraction.organizeWindows[1].lines)
            .isEqualTo(listOf("b0", "b1", "c0", "c1"))
    }

    @Test
    fun `skips left hemistich that has no right partner`() {
        val verses =
            listOf(
                VerseRow(vorder = 2, position = 1, text = "a1"),
                VerseRow(vorder = 3, position = 0, text = "b0"),
                VerseRow(vorder = 4, position = 1, text = "b1"),
            )

        val extraction =
            GameSessionPoolBuilder.extractFromVerses(
                poemId = 12,
                poetId = 2,
                poetName = "حافظ",
                verses = verses,
            )

        assertThat(extraction.distichs).hasSize(1)
        assertThat(extraction.distichs.first().firstHemistich).isEqualTo("b0")
        assertThat(extraction.distichs.first().secondHemistich).isEqualTo("b1")
        assertThat(extraction.organizeWindows).isEmpty()
    }

    @Test
    fun `organize requires two complete distichs`() {
        val verses =
            listOf(
                VerseRow(vorder = 1, position = 0, text = "a0"),
                VerseRow(vorder = 2, position = 1, text = "a1"),
                VerseRow(vorder = 3, position = 0, text = "b0"),
            )

        val extraction =
            GameSessionPoolBuilder.extractFromVerses(
                poemId = 13,
                poetId = 2,
                poetName = "حافظ",
                verses = verses,
            )

        assertThat(extraction.distichs).hasSize(1)
        assertThat(extraction.organizeWindows).isEmpty()
    }
}
