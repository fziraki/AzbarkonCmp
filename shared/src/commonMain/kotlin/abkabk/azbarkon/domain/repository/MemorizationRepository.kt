package abkabk.azbarkon.domain.repository

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.ActiveMemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import kotlinx.coroutines.flow.Flow

interface MemorizationRepository {
    fun observeActiveSummary(): Flow<MemorizationSummary>

    fun observePracticeStreak(): Flow<Int>

    suspend fun countReviewedVerses(): Int

    suspend fun getActivePoems(): Result<List<ActiveMemorizationPoem>, MemorizationError>

    suspend fun getCompletedPoems(): Result<List<ActiveMemorizationPoem>, MemorizationError>

    suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError>

    suspend fun addPoem(poemId: Int): EmptyResult<MemorizationError>

    suspend fun removePoem(poemId: Int): EmptyResult<MemorizationError>

    suspend fun getDueCards(poemId: Int? = null): Result<List<SrsCard>, MemorizationError>

    suspend fun getCardsByPoemId(poemId: Int): Result<List<SrsCard>, MemorizationError>

    suspend fun submitReview(
        cardId: Long,
        grade: SrsGrade,
    ): Result<SrsCard, MemorizationError>

    suspend fun submitPoemReview(
        poemId: Int,
        verseGrades: List<SrsGrade>,
        consecutiveEasy: Int,
    ): Result<Int, MemorizationError>

    suspend fun isPoemActive(poemId: Int): Boolean

    suspend fun resolveQuickStart(
        poetNameFragment: String,
        categoryTextFragment: String? = null,
    ): QuickStartTarget
}
