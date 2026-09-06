package abkabk.azbarkon.domain.datasource

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredActivePoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog

interface MemorizationLocalDataSource {
    suspend fun countActivePoems(): Int

    suspend fun isPoemActive(poemId: Int): Boolean

    suspend fun insertActivePoem(
        poemId: Int,
        addedAtMillis: Long,
        status: String,
    )

    suspend fun deleteActivePoem(poemId: Int)

    suspend fun getActivePoemIds(): List<Int>

    suspend fun getActivePoemAddedAt(poemId: Int): Long?

    suspend fun insertCards(cards: List<SrsCard>)

    suspend fun getCardById(cardId: Long): SrsCard?

    suspend fun getDueCards(
        nowMillis: Long,
        poemId: Int? = null,
    ): List<SrsCard>

    suspend fun getCardsByPoemId(poemId: Int): List<SrsCard>

    suspend fun countDueCards(
        nowMillis: Long,
        poemId: Int? = null,
    ): Int

    suspend fun updateCard(card: SrsCard)

    suspend fun updateCardsByPoemIdSchedule(
        poemId: Int,
        interval: Int,
        dueDateMillis: Long,
        score: Double,
    )

    suspend fun countCardsByPoemId(poemId: Int): Int

    suspend fun countReviewedCardsByPoemId(poemId: Int): Int

    suspend fun getMaxIntervalByPoemId(poemId: Int): Int

    suspend fun getReviewCountByPoemId(poemId: Int): Int

    suspend fun getActivePoemIdsByStatus(status: String): List<Int>

    suspend fun updatePoemStatus(
        poemId: Int,
        status: String,
    )

    suspend fun insertReviewLog(
        cardId: Long,
        grade: SrsGrade,
        previousInterval: Int,
        newInterval: Int,
        reviewTimeMillis: Long,
    )

    suspend fun getReviewDayKeys(): List<Int>

    suspend fun countReviewedVerses(): Int

    suspend fun dumpActivePoems(): List<StoredActivePoem>

    suspend fun dumpCards(): List<SrsCard>

    suspend fun dumpReviewLogs(): List<StoredReviewLog>

    suspend fun replaceAll(
        activePoems: List<StoredActivePoem>,
        cards: List<SrsCard>,
        reviewLogs: List<StoredReviewLog>,
    )

    suspend fun findPoetIdByName(nameFragment: String): Result<Int, DataError.Local>

    suspend fun findCategoryByPoetAndText(
        poetId: Int,
        textFragment: String,
    ): Result<Pair<Int, String>, DataError.Local>
}
