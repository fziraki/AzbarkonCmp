package abkabk.azbarkon.domain.datasource

import abkabk.azbarkon.core.domain.result.DataError
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.StoredPoem
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog

interface MemorizationLocalDataSource {
    suspend fun countActivePoems(): Int

    suspend fun isPoemActive(poemId: Int): Boolean

    suspend fun insertPoem(
        poemId: Int,
        addedAtMillis: Long,
        status: String,
        interval: Int = 0,
        dueDateMillis: Long = 0,
        consecutiveCorrect: Int = 0,
        totalCard: Int
    )

    suspend fun deletePoem(poemId: Int)

    suspend fun getPoemAddedAt(poemId: Int): Long?

    suspend fun getMemorizationPoem(poemId: Int): StoredPoem?

    suspend fun insertCards(cards: List<SrsCard>)

    suspend fun getCardById(cardId: Long): SrsCard?

    suspend fun getDueCards(
        poemId: Int,
    ): List<SrsCard>

    suspend fun getCardsByPoemId(poemId: Int): List<SrsCard>

    suspend fun countDueCards(
        poemId: Int,
    ): Int

    suspend fun updatePoemSchedule(
        poemId: Int,
        status: String,
        interval: Int,
        dueDateMillis: Long,
        consecutiveCorrect: Int,
    )

    suspend fun countCardsByPoemId(poemId: Int): Int
    suspend fun getLastReviewLogByPoemId(poemId: Int): StoredReviewLog
    suspend fun getPoemIdsByStatus(status: String): List<Int>

    suspend fun insertReviewLog(
        poemId: Int,
        reviewRound: Int,
        minTotalScore: Double,
        userTotalScore: Double,
        cardIndex: Int,
        sessionReviewed: Int,
        sessionMistakes: Int,
        sessionLearned: Int
    )

    suspend fun getReviewDayKeys(): List<Int>

    suspend fun countReviewedVerses(): Int

    suspend fun dumpActivePoems(): List<StoredPoem>

    suspend fun dumpCards(): List<SrsCard>

    suspend fun dumpReviewLogs(): List<StoredReviewLog>

    suspend fun replaceAll(
        activePoems: List<StoredPoem>,
        cards: List<SrsCard>,
        reviewLogs: List<StoredReviewLog>,
    )

    suspend fun findPoetIdByName(nameFragment: String): Result<Int, DataError.Local>

    suspend fun findCategoryByPoetAndText(
        poetId: Int,
        textFragment: String,
    ): Result<Pair<Int, String>, DataError.Local>
}
