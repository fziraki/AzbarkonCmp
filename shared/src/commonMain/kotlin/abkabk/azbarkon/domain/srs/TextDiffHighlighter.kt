package abkabk.azbarkon.domain.srs

enum class DiffTokenType {
    CORRECT,
    MISSING,
    WRONG,
}

data class DiffToken(
    val text: String,
    val type: DiffTokenType,
    val coveredWords: Int = 1,
)

object TextDiffHighlighter {
    private const val EASY_THRESHOLD = 0.95
    private const val GOOD_THRESHOLD = 0.75
    private const val HARD_THRESHOLD = 0.45
    private const val MAX_SPAN_LENGTH = 3

    fun normalizeForComparison(text: String): String {
        val withoutJoiners = text.replace('\u0640', ' ').replace('\u200C', ' ')
        val normalized = StringBuilder(withoutJoiners.length)
        withoutJoiners.forEach { char ->
            if (char.category != CharCategory.NON_SPACING_MARK) {
                normalized.append(normalizeArabicLetter(char))
            }
        }
        return normalized.toString()
    }

    private fun normalizeArabicLetter(char: Char): Char =
        when (char) {
            '\u0622', '\u0623', '\u0625' -> '\u0627'
            '\u064A' -> '\u06CC'
            '\u0643' -> '\u06A9'
            '\u0629' -> '\u0647'
            else -> char
        }

    fun extractAlphabeticLetters(text: String): String {
        val letters = StringBuilder()
        normalizeForComparison(text).forEach { char ->
            if (char.isLetter()) {
                letters.append(char)
            }
        }
        return letters.toString()
    }

    fun suggestGradeFromChars(
        expected: String,
        actual: String,
    ): abkabk.azbarkon.domain.model.memorization.SrsGrade {
        val actualLetters = extractAlphabeticLetters(actual)
        val expectedLetters = extractAlphabeticLetters(expected)
        if (expectedLetters.isNotEmpty() &&
            actualLetters.length > expectedLetters.length &&
            actualLetters.contains(expectedLetters)
        ) {
            return abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY
        }
        val diffTokens = diffUserWords(expected, actual)
        val expectedWords = splitDisplayWords(expected)
        if (expectedWords.isEmpty()) return abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY
        val coveredWordCount =
            diffTokens.filter { it.type == DiffTokenType.CORRECT }.sumOf { it.coveredWords }
        val coveredRatio = coveredWordCount.toDouble() / expectedWords.size
        return when {
            coveredRatio >= EASY_THRESHOLD -> abkabk.azbarkon.domain.model.memorization.SrsGrade.EASY
            coveredRatio >= GOOD_THRESHOLD -> abkabk.azbarkon.domain.model.memorization.SrsGrade.GOOD
            coveredRatio >= HARD_THRESHOLD -> abkabk.azbarkon.domain.model.memorization.SrsGrade.HARD
            else -> abkabk.azbarkon.domain.model.memorization.SrsGrade.AGAIN
        }
    }

    fun diffUserWords(expected: String, actual: String): List<DiffToken> {
        val expectedWords = splitDisplayWords(expected)
        val actualWords = splitDisplayWords(actual)
        if (actualWords.isEmpty()) return emptyList()

        val diffTokens = mutableListOf<DiffToken>()
        var expectedWordIndex = 0
        for (typedWord in actualWords) {
            expectedWordIndex = appendTokenForWord(diffTokens, expectedWords, typedWord, expectedWordIndex)
        }
        for (i in expectedWordIndex until expectedWords.size) {
            diffTokens.add(DiffToken(expectedWords[i], DiffTokenType.MISSING))
        }
        return diffTokens
    }

    private fun appendTokenForWord(
        diffTokens: MutableList<DiffToken>,
        expectedWords: List<String>,
        typedWord: String,
        expectedWordIndex: Int,
    ): Int {
        val typedWordLetters = extractAlphabeticLetters(typedWord)
        if (typedWordLetters.isEmpty()) {
            diffTokens.add(DiffToken(typedWord, DiffTokenType.WRONG))
            return expectedWordIndex
        }
        val coveredWordCount = matchSpan(expectedWords, typedWordLetters, expectedWordIndex)
        if (coveredWordCount > 0) {
            diffTokens.add(DiffToken(typedWord, DiffTokenType.CORRECT, coveredWordCount))
            return expectedWordIndex + coveredWordCount
        }
        val matchedIndex =
            (expectedWordIndex + 1..expectedWords.lastIndex).firstOrNull { i ->
                matchSpan(expectedWords, typedWordLetters, i) > 0
            }
        if (matchedIndex != null) {
            for (i in expectedWordIndex until matchedIndex) {
                diffTokens.add(DiffToken(expectedWords[i], DiffTokenType.MISSING))
            }
            val matchedSpanLength = matchSpan(expectedWords, typedWordLetters, matchedIndex)
            diffTokens.add(DiffToken(typedWord, DiffTokenType.CORRECT, matchedSpanLength))
            return matchedIndex + matchedSpanLength
        }
        diffTokens.add(DiffToken(typedWord, DiffTokenType.WRONG))
        return if (expectedWordIndex < expectedWords.size) expectedWordIndex + 1 else expectedWordIndex
    }

    private fun matchSpan(expectedWords: List<String>, typedWordLetters: String, startIndex: Int): Int {
        if (startIndex >= expectedWords.size) return 0
        for (spanLength in 1..MAX_SPAN_LENGTH) {
            val spanEnd = startIndex + spanLength
            if (spanEnd > expectedWords.size) break
            val spanLetters = expectedWords.subList(startIndex, spanEnd).joinToString("") { extractAlphabeticLetters(it) }
            if (spanLetters.equals(typedWordLetters, ignoreCase = true)) return spanLength
        }
        return 0
    }

    private fun splitDisplayWords(text: String): List<String> =
        text
            .replace('\u0640', ' ')
            .replace('\u200C', ' ')
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

}
