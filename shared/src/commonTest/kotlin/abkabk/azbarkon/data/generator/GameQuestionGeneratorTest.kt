package abkabk.azbarkon.data.generator

import abkabk.azbarkon.domain.model.Poet
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNotEqualTo
import kotlin.test.Test

class GameQuestionGeneratorTest {
    @Test
    fun `next verse builds four shuffled options with one correct answer`() {
        val question =
            GameQuestionGenerator.buildNextVerseQuestion(
                promptLine = "مرا هزار امید است",
                poetName = "حافظ",
                correctAnswer = "تویی",
                distractors = listOf("غمی", "بهار", "صدا"),
                seed = 42L,
            )

        assertThat(question).isNotNull()
        assertThat(question!!.options).hasSize(4)
        assertThat(question.options[question.correctIndex]).isEqualTo("تویی")
    }

    @Test
    fun `complete poem builds four unique options including both correct words`() {
        val question =
            GameQuestionGenerator.buildCompletePoemQuestion(
                rightHemistich = "ز خاک کوی تو",
                leftHemistich = "نسیم سحر است و عطر جانان",
                poetName = "حافظ",
                poemWords = listOf("نسیم", "سحر", "است", "و", "عطر", "جانان", "ز", "خاک"),
                seed = 7L,
            )

        assertThat(question).isNotNull()
        assertThat(question!!.poetName).isEqualTo("حافظ")
        assertThat(question!!.options).hasSize(4)
        assertThat(question.options).contains(question.correctWords.first)
        assertThat(question.options).contains(question.correctWords.second)
    }

    @Test
    fun `organize poem shuffles lines away from correct order`() {
        val question =
            GameQuestionGenerator.buildOrganizePoemQuestion(
                poemId = 1,
                startVorder = 3,
                poetName = "حافظ",
                lines = listOf("a", "b", "c", "d"),
                seed = 99L,
            )

        assertThat(question).isNotNull()
        assertThat(question!!.poetName).isEqualTo("حافظ")
        assertThat(question.lines.map { it.id }).isNotEqualTo(question.correctOrder)
    }

    @Test
    fun `find poet includes correct poet in options`() {
        val hafez = Poet(id = 2, name = "حافظ", description = "", rootCatId = 1, imageUrl = null)
        val others =
            listOf(
                Poet(id = 3, name = "سعدی", description = "", rootCatId = 1, imageUrl = null),
                Poet(id = 4, name = "فردوسی", description = "", rootCatId = 1, imageUrl = null),
                Poet(id = 5, name = "مولوی", description = "", rootCatId = 1, imageUrl = null),
            )

        val question =
            GameQuestionGenerator.buildFindPoetQuestion(
                rightHemistich = "بیت اول",
                leftHemistich = "بیت دوم",
                correctPoet = hafez,
                allPoets = listOf(hafez) + others,
                seed = 11L,
            )

        assertThat(question).isNotNull()
        assertThat(question!!.options).hasSize(4)
        assertThat(question.options.map { it.id }).contains(2)
    }
}
