package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.domain.model.PoemVerse
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test

class CardGeneratorTest {
    @Test
    fun `each verse becomes a masked card`() {
        val verses =
            listOf(
                PoemVerse(poemId = 1, vorder = 1, position = 0, text = "مصرع اول"),
                PoemVerse(poemId = 1, vorder = 2, position = 1, text = "مصرع دوم"),
                PoemVerse(poemId = 1, vorder = 3, position = 0, text = "بیت دوم راست"),
                PoemVerse(poemId = 1, vorder = 4, position = 1, text = "بیت دوم چپ"),
            )

        val cards = CardGenerator.buildGeneratedCards(verses)

        assertThat(cards).hasSize(4)
        assertThat(cards[0].front).isEqualTo("مصرع اول")
        assertThat(cards[0].back).isEqualTo("مصرع اول")
        assertThat(cards[1].back).isEqualTo("مصرع دوم")
    }

    @Test
    fun `single line poem creates masked front`() {
        val verses =
            listOf(
                PoemVerse(poemId = 1, vorder = 0, position = 0, text = "یک دو سه چهار"),
            )

        val cards = CardGenerator.buildGeneratedCards(verses)

        assertThat(cards).hasSize(1)
        assertThat(cards[0].front.contains("...")).isTrue()
    }

    @Test
    fun `expectedContinuation returns hidden words for masked single line`() {
        val front = "یک دو ..."
        val back = "یک دو سه چهار"

        assertThat(CardGenerator.expectedContinuation(front, back)).isEqualTo("سه چهار")
    }

    @Test
    fun `revealedFrontParts replaces masked ellipsis`() {
        val parts =
            CardGenerator.revealedFrontParts(
                front = "یک دو ...",
                continuation = "سه چهار",
            )

        assertThat(parts.prefix).isEqualTo("یک دو ")
        assertThat(parts.continuation).isEqualTo("سه چهار")
        assertThat(parts.suffix).isEqualTo("")
    }
}
