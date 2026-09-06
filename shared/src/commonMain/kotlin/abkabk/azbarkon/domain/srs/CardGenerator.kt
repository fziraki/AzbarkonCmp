package abkabk.azbarkon.domain.srs

import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.domain.model.PoemVerse
import abkabk.azbarkon.domain.model.memorization.SrsCard

data class GeneratedCard(
    val cardIndex: Int,
    val front: String,
    val back: String,
)

object CardGenerator {
    fun generateCards(
        poemId: Int,
        verses: List<PoemVerse>,
        nowMillis: Long = currentTimeMillis(),
    ): List<SrsCard> {
        val generated = buildGeneratedCards(verses)
        return generated.map { card ->
            SrsCard(
                id = 0,
                poemId = poemId,
                cardIndex = card.cardIndex,
                front = card.front,
                back = card.back,
                interval = 0,
                dueDateMillis = nowMillis,
                consecutiveCorrect = 0,
                score = 0.0,
            )
        }
    }

    fun buildGeneratedCards(verses: List<PoemVerse>): List<GeneratedCard> {
        if (verses.isEmpty()) return emptyList()

        val grouped =
            verses
                .sortedWith(compareBy({ it.vorder }, { it.position }))
                .groupBy { it.vorder }

        return grouped.entries
            .sortedBy { it.key }
            .mapIndexed { index, (_, groupVerses) ->
                val line = groupVerses.first().text

                GeneratedCard(
                    cardIndex = index,
                    front = maskWords(line),
                    back = line,
                )
            }
    }

    private fun maskWords(line: String): String {
        val words = line.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size <= 2) return line
        val visibleCount = (words.size / 2).coerceAtLeast(1)
        return words.take(visibleCount).joinToString(" ") + " ..."
    }

    fun expectedContinuation(front: String, back: String): String {
        if (front.trimEnd().endsWith("...")) {
            val visiblePart = front.replace(Regex("\\s*\\.\\.\\.\\s*$"), "").trim()
            val visibleWords = visiblePart.split(Regex("\\s+")).filter { it.isNotBlank() }
            val allWords = back.split(Regex("\\s+")).filter { it.isNotBlank() }
            return allWords.drop(visibleWords.size).joinToString(" ")
        }
        return back.trim()
    }

    data class RevealedFrontParts(
        val prefix: String,
        val continuation: String,
        val suffix: String = "",
    )

    fun revealedFrontParts(front: String, continuation: String): RevealedFrontParts {
        if (front.trimEnd().endsWith("...")) {
            val prefix = front.replace(Regex("\\s*\\.\\.\\.\\s*$"), "").trimEnd()
            val separator = if (prefix.isEmpty()) "" else " "
            return RevealedFrontParts(prefix = "$prefix$separator", continuation = continuation)
        }
        return RevealedFrontParts(prefix = "", continuation = continuation)
    }
}
