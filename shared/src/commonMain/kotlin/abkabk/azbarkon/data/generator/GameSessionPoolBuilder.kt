package abkabk.azbarkon.data.generator

import abkabk.azbarkon.domain.model.games.GameConstants
import abkabk.azbarkon.domain.model.games.GameDistichCandidate
import abkabk.azbarkon.domain.model.games.GameOrganizeWindow
import abkabk.azbarkon.domain.model.games.GameType

internal data class VerseRow(
    val vorder: Long,
    val position: Long,
    val text: String,
)

internal data class PoemPoolExtraction(
    val distichs: List<GameDistichCandidate>,
    val organizeWindows: List<GameOrganizeWindow>,
    val poemWords: List<String>,
)

internal object GameSessionPoolBuilder {
    // Ganjoor DB stores each verse line as a separate row with a unique vorder.
    // Position 0 = right/first hemistich, position 1 = left/second hemistich.
    // A distich (بیت) = two consecutive vorders: (N position=0, N+1 position=1).

    fun isPoemAcceptable(
        gameType: GameType,
        distichCount: Int,
        organizeCount: Int,
        totalVerses: Int,
    ): Boolean {
        if (totalVerses > 0 && distichCount * 2 < totalVerses) return false
        return when (gameType) {
            GameType.ORGANIZE_POEM -> organizeCount > 0
            GameType.NEXT_VERSE,
            GameType.FIND_POET,
            GameType.COMPLETE_POEM,
            -> distichCount >= GameConstants.POOL_MIN_DISTICHS_PER_POEM
        }
    }

    fun extractFromVerses(
        poemId: Long,
        poetId: Long,
        poetName: String,
        verses: List<VerseRow>,
    ): PoemPoolExtraction {
        val poemWords =
            verses
                .flatMap { verse -> verse.text.split(Regex("\\s+")).filter { it.isNotBlank() } }
                .distinct()

        val versesByVorder = verses.groupBy { it.vorder }
        val distichs = extractDistichs(poemId, poetId, poetName, versesByVorder)
        val organizeWindows = extractOrganizeWindows(poemId, versesByVorder)

        return PoemPoolExtraction(
            distichs = distichs,
            organizeWindows = organizeWindows,
            poemWords = poemWords,
        )
    }

    private fun extractDistichs(
        poemId: Long,
        poetId: Long,
        poetName: String,
        versesByVorder: Map<Long, List<VerseRow>>,
    ): List<GameDistichCandidate> =
        versesByVorder.keys
            .sorted()
            .mapNotNull { vorder ->
                val lines = extractHemistichs(versesByVorder, vorder) ?: return@mapNotNull null
                GameDistichCandidate(
                    poemId = poemId,
                    vorder = vorder,
                    firstHemistich = lines[0],
                    secondHemistich = lines[1],
                    poetId = poetId,
                    poetName = poetName,
                )
            }

    /** Extracts windows of 2 consecutive distichs (4 hemistichs) for the organize-poem game. */
    private fun extractOrganizeWindows(
        poemId: Long,
        versesByVorder: Map<Long, List<VerseRow>>,
    ): List<GameOrganizeWindow> =
        versesByVorder.keys
            .sorted()
            .mapNotNull { startVorder ->
                val firstDistich = extractHemistichs(versesByVorder, startVorder)
                    ?: return@mapNotNull null
                val secondDistich = extractHemistichs(versesByVorder, startVorder + 2)
                    ?: return@mapNotNull null
                GameOrganizeWindow(
                    poemId = poemId.toInt(),
                    startVorder = startVorder.toInt(),
                    lines = firstDistich + secondDistich,
                )
            }

    /**
     * Returns the two hemistichs (right + left) of the distich starting at [vorder],
     * or null if the verse at [vorder] is the left hemistich of a previous distich.
     */
    private fun extractHemistichs(
        versesByVorder: Map<Long, List<VerseRow>>,
        vorder: Long,
    ): List<String>? {
        if (isSecondHemistich(versesByVorder, vorder)) return null
        val first = versesByVorder[vorder]?.find { it.position == 0L }?.text ?: return null
        val second = versesByVorder[vorder + 1]?.find { it.position == 1L }?.text ?: return null
        if (first.length > GameConstants.MAX_HEMISTICH_LENGTH ||
            second.length > GameConstants.MAX_HEMISTICH_LENGTH
        ) {
            return null
        }
        return listOf(first, second)
    }

    /**
     * True if [vorder] is the left hemistich (position=1) of a distich
     * whose right hemistich is at vorder-1. These verses should not start a new distich.
     */
    private fun isSecondHemistich(
        versesByVorder: Map<Long, List<VerseRow>>,
        vorder: Long,
    ): Boolean {
        val currentRows = versesByVorder[vorder] ?: return false
        val previousRows = versesByVorder[vorder - 1] ?: return false
        return currentRows.none { it.position == 0L } &&
            currentRows.any { it.position == 1L } &&
            previousRows.any { it.position == 0L }
    }
}
