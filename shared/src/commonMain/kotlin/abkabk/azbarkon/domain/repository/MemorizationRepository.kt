package abkabk.azbarkon.domain.repository

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import kotlinx.coroutines.flow.Flow

interface MemorizationRepository {
    fun observeActiveSummary(): Flow<MemorizationSummary>

    fun observePracticeStreak(): Flow<Int>

    suspend fun countReviewedVerses(): Int

    suspend fun getPoemsByStatus(status: MemorizationStatus): Result<List<MemorizationPoem>, MemorizationError>

    suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError>

    suspend fun addPoem(poemId: Int): EmptyResult<MemorizationError>

    suspend fun removePoem(poemId: Int): EmptyResult<MemorizationError>

    suspend fun getDueCards(poemId: Int): Result<List<SrsCard>, MemorizationError>

    suspend fun getCardsByPoemId(poemId: Int): Result<List<SrsCard>, MemorizationError>

    suspend fun insertReviewLog(
        poemId: Int,
        cardId: Long,
        grade: SrsGrade,
        minTotalScore: Double,
        cardIndex: Int,
        sessionReviewed: Int,
        sessionMistakes: Int,
        sessionLearned: Int
    ): Result<Unit, MemorizationError>

    suspend fun submitPoemReview(poemId: Int): Result<Int, MemorizationError>

    suspend fun isPoemActive(poemId: Int): Boolean

    suspend fun resolveQuickStart(
        poetNameFragment: String,
        categoryTextFragment: String? = null,
    ): QuickStartTarget

    suspend fun getLastReviewLog(poemId: Int): StoredReviewLog

}
