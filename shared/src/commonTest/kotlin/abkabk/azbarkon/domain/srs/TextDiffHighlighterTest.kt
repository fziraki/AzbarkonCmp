package abkabk.azbarkon.domain.srs

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TextDiffHighlighterTest {
    @Test
    fun `ignores arabic diacritics when comparing`() {
        val withDiacritics = "بِخُرْ"
        val withoutDiacritics = "بخر"

        val grade = TextDiffHighlighter.suggestGradeFromChars(withDiacritics, withoutDiacritics)

        assertThat(grade).isEqualTo(abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY)
    }

    @Test
    fun `suggestGradeFromChars ignores zwnj vs plain space`() {
        val grade = TextDiffHighlighter.suggestGradeFromChars("مشکل\u200Cها", "مشکل ها")

        assertThat(grade).isEqualTo(abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY)
    }

    @Test
    fun `suggestGradeFromChars lowers grade for typo and skipped words`() {
        val grade =
            TextDiffHighlighter.suggestGradeFromChars(
                "ادر کاسا و ناولها",
                "اشر کاساس ناولها",
            )

        assertThat(grade).isEqualTo(abkabk.azbarkon.domain.model.memorization.SrsGrade.AGAIN)
    }

    @Test
    fun `normalizeForComparison strips combining marks`() {
        val normalized = TextDiffHighlighter.normalizeForComparison("بِخُرْ")

        assertThat(normalized).isEqualTo("بخر")
    }

    @Test
    fun `char match ignores spaces and punctuation`() {
        val grade =
            TextDiffHighlighter.suggestGradeFromChars(
                expected = "ولی افتاد مشکل‌ها",
                actual = "ولی، افتاد! مشکلها",
            )

        assertThat(grade).isEqualTo(abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY)
    }

    @Test
    fun `char match partial answer suggests lower grade`() {
        val grade =
            TextDiffHighlighter.suggestGradeFromChars(
                expected = "ولی افتاد مشکل‌ها",
                actual = "ولی",
            )

        assertThat(grade).isEqualTo(abkabk.azbarkon.domain.model.memorization.SrsGrade.AGAIN)
    }

    @Test
    fun `diffUserWords colors user words only`() {
        val tokens = TextDiffHighlighter.diffUserWords("مصرع دوم", "مصرع دوم")

        assertThat(tokens).isEqualTo(
            listOf(
                DiffToken("مصرع", DiffTokenType.CORRECT),
                DiffToken("دوم", DiffTokenType.CORRECT),
            ),
        )
    }

    @Test
    fun `diffUserWords marks wrong user word`() {
        val tokens = TextDiffHighlighter.diffUserWords("مصرع دوم", "مصرع غلط")

        assertThat(tokens[0].type).isEqualTo(DiffTokenType.CORRECT)
        assertThat(tokens[1].type).isEqualTo(DiffTokenType.WRONG)
    }

    @Test
    fun `diffUserWords keeps later words green after a wrong word`() {
        val tokens = TextDiffHighlighter.diffUserWords("ادر کاسا و ناولها", "در کاسا و ناولها")

        assertThat(tokens.map { it.type }).isEqualTo(
            listOf(
                DiffTokenType.WRONG,
                DiffTokenType.CORRECT,
                DiffTokenType.CORRECT,
                DiffTokenType.CORRECT,
            ),
        )
    }

    @Test
    fun `diffUserWords colors typo red and keeps the rest green`() {
        val tokens =
            TextDiffHighlighter.diffUserWords(
                "ادر کاسا و ناولها",
                "اشر کاساس ناولها",
            )

        assertThat(tokens).isEqualTo(
            listOf(
                DiffToken("اشر", DiffTokenType.WRONG),
                DiffToken("کاساس", DiffTokenType.WRONG),
                DiffToken("و", DiffTokenType.MISSING),
                DiffToken("ناولها", DiffTokenType.CORRECT),
            ),
        )
    }

    @Test
    fun `diffUserWords colors skipped word as missing and keeps the rest green`() {
        val tokens =
            TextDiffHighlighter.diffUserWords(
                "ادر کاسا و ناولها",
                "ادر ناولها",
            )

        assertThat(tokens).isEqualTo(
            listOf(
                DiffToken("ادر", DiffTokenType.CORRECT),
                DiffToken("کاسا", DiffTokenType.MISSING),
                DiffToken("و", DiffTokenType.MISSING),
                DiffToken("ناولها", DiffTokenType.CORRECT),
            ),
        )
    }

    @Test
    fun `diffUserWords shows remaining words as missing after a short answer`() {
        val tokens =
            TextDiffHighlighter.diffUserWords(
                "ادر کاسا و ناولها",
                "ادر",
            )

        assertThat(tokens).isEqualTo(
            listOf(
                DiffToken("ادر", DiffTokenType.CORRECT),
                DiffToken("کاسا", DiffTokenType.MISSING),
                DiffToken("و", DiffTokenType.MISSING),
                DiffToken("ناولها", DiffTokenType.MISSING),
            ),
        )
    }

    @Test
    fun `diffUserWords colors longer and shorter words red and rest green`() {
        val tokens =
            TextDiffHighlighter.diffUserWords(
                "ادر کاسا و ناولها",
                "اد کاساس و ناول",
            )

        assertThat(tokens.map { it.type }).isEqualTo(
            listOf(
                DiffTokenType.WRONG,
                DiffTokenType.WRONG,
                DiffTokenType.CORRECT,
                DiffTokenType.WRONG,
            ),
        )
    }

    @Test
    fun `diffUserWords ignores extra spaces between words`() {
        val tokens = TextDiffHighlighter.diffUserWords("سه چهار", "سه  چهار")

        assertThat(tokens).isEqualTo(
            listOf(
                DiffToken("سه", DiffTokenType.CORRECT),
                DiffToken("چهار", DiffTokenType.CORRECT),
            ),
        )
    }

    @Test
    fun `diffUserWords matches letters despite punctuation in actual`() {
        val tokens = TextDiffHighlighter.diffUserWords("ولی افتاد", "ولی، افتاد!")

        assertThat(tokens.all { it.type == DiffTokenType.CORRECT }).isEqualTo(true)
    }

    @Test
    fun `diffUserWords ignores zwnj vs plain space`() {
        val tokens = TextDiffHighlighter.diffUserWords("مشکل\u200Cها", "مشکل ها")

        assertThat(tokens.all { it.type == DiffTokenType.CORRECT }).isEqualTo(true)
    }
}
