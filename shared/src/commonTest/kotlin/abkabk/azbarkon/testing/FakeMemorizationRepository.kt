package abkabk.azbarkon.testing

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.ActiveMemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.repository.MemorizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMemorizationRepository : MemorizationRepository {
    var summary = MemorizationSummary(activePoemCount = 0, dueCardsToday = 0)
    var activePoems: Result<List<ActiveMemorizationPoem>, MemorizationError> = Result.Success(emptyList())
    var dueCards: Result<List<SrsCard>, MemorizationError> = Result.Success(emptyList())
    var addPoemResult: EmptyResult<MemorizationError> = Result.Success(Unit)
    var reviewResult: Result<SrsCard, MemorizationError> = Result.Error(MemorizationError.CardNotFound)
    var isActive: Boolean = false
    var lastAddedPoemId: Int? = null
    var lastReviewedCardId: Long? = null
    var lastReviewGrade: SrsGrade? = null

    private val summaryFlow = MutableStateFlow(summary)
    private val streakFlow = MutableStateFlow(0)
    var practiceStreak: Int = 0
        set(value) {
            field = value
            streakFlow.value = value
        }
    var reviewedVersesCount: Int = 0

    override fun observeActiveSummary(): Flow<MemorizationSummary> = summaryFlow

    override fun observePracticeStreak(): Flow<Int> = streakFlow

    override suspend fun countReviewedVerses(): Int = reviewedVersesCount

    fun emitSummary(value: MemorizationSummary) {
        summary = value
        summaryFlow.value = value
    }

    override suspend fun getActivePoems(): Result<List<ActiveMemorizationPoem>, MemorizationError> = activePoems

    override suspend fun getCompletedPoems(): Result<List<ActiveMemorizationPoem>, MemorizationError> = activePoems

    override suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError> = Result.Success(Unit)

    override suspend fun addPoem(poemId: Int): EmptyResult<MemorizationError> {
        lastAddedPoemId = poemId
        return addPoemResult
    }

    override suspend fun removePoem(poemId: Int): EmptyResult<MemorizationError> = Result.Success(Unit)

    override suspend fun getDueCards(poemId: Int?): Result<List<SrsCard>, MemorizationError> = dueCards

    override suspend fun getCardsByPoemId(poemId: Int): Result<List<SrsCard>, MemorizationError> = dueCards

    override suspend fun submitReview(
        cardId: Long,
        grade: SrsGrade,
    ): Result<SrsCard, MemorizationError> {
        lastReviewedCardId = cardId
        lastReviewGrade = grade
        return reviewResult
    }

    override suspend fun submitPoemReview(
        poemId: Int,
        verseGrades: List<SrsGrade>,
        consecutiveEasy: Int,
    ): Result<Int, MemorizationError> = Result.Success(1)

    override suspend fun isPoemActive(poemId: Int): Boolean = isActive

    override suspend fun resolveQuickStart(
        poetNameFragment: String,
        categoryTextFragment: String?,
    ): QuickStartTarget = QuickStartTarget()
}
