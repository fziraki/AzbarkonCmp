package abkabk.azbarkon.testing

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.domain.model.memorization.MemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import abkabk.azbarkon.domain.repository.MemorizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMemorizationRepository : MemorizationRepository {
    var summary = MemorizationSummary(activePoemCount = 0, dueCardsToday = 0)
    var activePoems: Result<List<MemorizationPoem>, MemorizationError> = Result.Success(emptyList())
    var dueCards: Result<List<SrsCard>, MemorizationError> = Result.Success(emptyList())
    var addPoemResult: EmptyResult<MemorizationError> = Result.Success(Unit)
    var isActive: Boolean = false
    var lastAddedPoemId: Int? = null
    var lastReviewedCardId: Long? = null
    var lastReviewGrade: SrsGrade? = null
    var lastPoemReviewPoemId: Int? = null
    var lastReviewLog: StoredReviewLog = StoredReviewLog(
        id = -1, poemId = 0, reviewRound = 0, minTotalScore = 0.0,
        userTotalScore = 0.0, cardIndex = 0, sessionReviewed = 0,
        sessionMistakes = 0, sessionLearned = 0,
    )

    private val summaryFlow = MutableStateFlow(summary)
    var reviewedVersesCount: Int = 0

    override fun observeActiveSummary(): Flow<MemorizationSummary> = summaryFlow

    override suspend fun countReviewedVerses(): Int = reviewedVersesCount

    fun emitSummary(value: MemorizationSummary) {
        summary = value
        summaryFlow.value = value
    }

    override suspend fun getPoemsByStatus(status: MemorizationStatus): Result<List<MemorizationPoem>, MemorizationError> = activePoems

    override suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError> = Result.Success(Unit)

    override suspend fun addPoem(poemId: Int): EmptyResult<MemorizationError> {
        lastAddedPoemId = poemId
        return addPoemResult
    }

    override suspend fun removePoem(poemId: Int): EmptyResult<MemorizationError> = Result.Success(Unit)

    override suspend fun getDueCards(poemId: Int): Result<List<SrsCard>, MemorizationError> = dueCards

    override suspend fun getCardsByPoemId(poemId: Int): Result<List<SrsCard>, MemorizationError> = dueCards

    override suspend fun insertReviewLog(
        poemId: Int,
        cardId: Long,
        grade: SrsGrade,
        minTotalScore: Double,
        cardIndex: Int,
        sessionReviewed: Int,
        sessionMistakes: Int,
        sessionLearned: Int
    ): Result<Unit, MemorizationError> {
        lastReviewedCardId = cardId
        lastReviewGrade = grade
        lastPoemReviewPoemId = poemId
        return Result.Success(Unit)
    }

    override suspend fun submitPoemReview(poemId: Int): Result<Int, MemorizationError> {
        lastPoemReviewPoemId = poemId
        return Result.Success(1)
    }

    override suspend fun isPoemActive(poemId: Int): Boolean = isActive

    override suspend fun resolveQuickStart(
        poetNameFragment: String,
        categoryTextFragment: String?,
    ): QuickStartTarget = QuickStartTarget()

    override suspend fun getLastReviewLog(poemId: Int): StoredReviewLog = lastReviewLog
}
