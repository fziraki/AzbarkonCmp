package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.util.consecutiveDayStreak
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.model.memorization.StoredReviewLog
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.repository.PoemRepository
import abkabk.azbarkon.domain.srs.CardGenerator
import abkabk.azbarkon.domain.srs.SrsScheduler
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class OfflineFirstMemorizationRepository(
    private val localDataSource: MemorizationLocalDataSource,
    private val poemRepository: PoemRepository,
    private val reviewNotificationCoordinator: MemorizationReviewNotificationCoordinator,
) : MemorizationRepository {
    private val summaryRefresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val streakRefresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun observeActiveSummary(): Flow<MemorizationSummary> =
        summaryRefresh
            .onStart { emit(Unit) }
            .map { loadSummary() }

    override fun observePracticeStreak(): Flow<Int> =
        streakRefresh
            .onStart { emit(Unit) }
            .map { loadPracticeStreak() }

    override suspend fun countReviewedVerses(): Int = localDataSource.countReviewedVerses()

    override suspend fun getPoemsByStatus(status: MemorizationStatus): Result<List<MemorizationPoem>, MemorizationError> =
        try {
            val now = currentTimeMillis()
            val poemIds = localDataSource.getPoemIdsByStatus(status.name)
            val poems =
                poemIds.mapNotNull { poemId ->
                    buildMemorizationPoem(
                        poemId = poemId,
                        nowMillis = now,
                        status = status
                    )
                }
            Result.Success(poems)
        } catch (e: IllegalStateException) {
            Napier.e("getPoems failed", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError> =
        try {
            localDataSource.deletePoem(poemId)
            addPoem(poemId)
        }catch (e: IllegalStateException){
            Napier.e("resetPoemToActive failed", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun addPoem(poemId: Int): EmptyResult<MemorizationError> {
        if (localDataSource.isPoemActive(poemId)) {
            notifySummaryChanged()
            return Result.Success(Unit)
        }

        if (localDataSource.countActivePoems() >= MAX_ACTIVE_POEMS) {
            return Result.Error(MemorizationError.MaxActivePoemsReached)
        }

        return when (val detailResult = poemRepository.getPoemDetail(poemId)) {
            is Result.Error -> Result.Error(MemorizationError.PoemNotFound)
            is Result.Success -> {
                val now = currentTimeMillis()
                val cards = CardGenerator.generateCards(poemId, detailResult.data.verses.filter { it.position >= 0 })
                if (cards.isEmpty()) {
                    Result.Error(MemorizationError.PoemNotFound)
                } else {
                    localDataSource.insertPoem(
                        poemId = poemId,
                        addedAtMillis = now,
                        status = MemorizationStatus.ACTIVE.name,
                        interval = 1,
                        dueDateMillis = now,
                        consecutiveCorrect = 0,
                        totalCard = cards.size
                    )
                    localDataSource.insertCards(cards)
                    notifySummaryChanged()
                    syncReviewNotifications()
                    Result.Success(Unit)
                }
            }
        }
    }

    override suspend fun removePoem(poemId: Int): EmptyResult<MemorizationError> =
        try {
            localDataSource.deletePoem(poemId)
            notifySummaryChanged()
            syncReviewNotifications()
            Result.Success(Unit)
        } catch (e: IllegalStateException) {
            Napier.e("removePoem failed for poemId=$poemId", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun getDueCards(poemId: Int): Result<List<SrsCard>, MemorizationError> =
        try {
            val cards = localDataSource.getDueCards(poemId)
            Result.Success(cards)
        } catch (e: IllegalStateException) {
            Napier.e("getDueCards failed for poemId=$poemId", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun getCardsByPoemId(poemId: Int): Result<List<SrsCard>, MemorizationError> =
        try {
            val cards = localDataSource.getCardsByPoemId(poemId)
            Result.Success(cards)
        } catch (e: IllegalStateException) {
            Napier.e("getCardsByPoemId failed for poemId=$poemId", e)
            Result.Error(MemorizationError.Unknown)
        }

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

        val lastReviewLog = localDataSource.getLastReviewLogByPoemId(poemId = poemId)
        val newScore = SrsScheduler.getNewScoreFromGradeEnum(
            currentScore = lastReviewLog.userTotalScore, grade = grade)

        val currentReviewRound = lastReviewLog.reviewRound
        val newReviewRound = if (cardIndex == 0) currentReviewRound + 1 else currentReviewRound

        localDataSource.insertReviewLog(
            poemId = poemId,
            reviewRound = newReviewRound,
            minTotalScore = minTotalScore,
            userTotalScore = newScore,
            cardIndex = cardIndex,
            sessionReviewed = sessionReviewed,
            sessionMistakes = sessionMistakes,
            sessionLearned = sessionLearned
        )

        notifySummaryChanged()
        syncReviewNotifications()
        return Result.Success(Unit)
    }

    override suspend fun submitPoemReview(
        poemId: Int,
    ): Result<Int, MemorizationError> {

        val lastReviewLog = localDataSource.getLastReviewLogByPoemId(poemId = poemId)
        val poem = localDataSource.getMemorizationPoem(poemId) ?: return Result.Error(MemorizationError.CardNotFound)

        val result = SrsScheduler.calculatePoemInterval(
            minTotalScore = lastReviewLog.minTotalScore,
            userTotalScore = lastReviewLog.userTotalScore,
            consecutiveEasy = poem.consecutiveCorrect
        )

        localDataSource.updatePoemSchedule(
            poemId = poemId,
            status = if (result.consecutiveEasy >= COMPLETION_THRESHOLD) {
                MemorizationStatus.COMPLETED.name }else{
                MemorizationStatus.ACTIVE.name
            },
            interval = result.interval,
            dueDateMillis = result.dueDateMillis,
            consecutiveCorrect = result.consecutiveEasy
        )
        notifySummaryChanged()
        syncReviewNotifications()
        return Result.Success(result.interval)
    }

    override suspend fun isPoemActive(poemId: Int): Boolean = localDataSource.isPoemActive(poemId)

    override suspend fun resolveQuickStart(
        poetNameFragment: String,
        categoryTextFragment: String?,
    ): QuickStartTarget {
        val poetResult = localDataSource.findPoetIdByName("%$poetNameFragment%")
        val poetId =
            when (poetResult) {
                is Result.Success -> poetResult.data
                is Result.Error -> return QuickStartTarget()
            }

        if (categoryTextFragment == null) {
            return QuickStartTarget(poetId = poetId)
        }

        val categoryResult =
            localDataSource.findCategoryByPoetAndText(
                poetId = poetId,
                textFragment = "%$categoryTextFragment%",
            )
        return when (categoryResult) {
            is Result.Success -> {
                val (catId, title) = categoryResult.data
                QuickStartTarget(poetId = poetId, catId = catId, catTitle = title)
            }
            is Result.Error -> QuickStartTarget(poetId = poetId)
        }
    }

    private suspend fun loadSummary(): MemorizationSummary {
        val poemIds = localDataSource.getPoemIdsByStatus(MemorizationStatus.ACTIVE.name)
        val dueCardsToday = poemIds.sumOf { localDataSource.countDueCards(it) }
        return MemorizationSummary(
            activePoemCount = poemIds.size,
            dueCardsToday = dueCardsToday,
        )
    }

    override suspend fun getLastReviewLog(poemId: Int): StoredReviewLog =
        localDataSource.getLastReviewLogByPoemId(poemId)

    private suspend fun loadPracticeStreak(): Int {
        val dayKeys = localDataSource.getReviewDayKeys()
        return consecutiveDayStreak(dayKeys)
    }

    private suspend fun buildMemorizationPoem(
        poemId: Int,
        nowMillis: Long,
        status: MemorizationStatus
    ): MemorizationPoem? {
        val detail =
            poemRepository.getPoemDetail(poemId).let { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> return null
                }
            }
        val poem = localDataSource.getMemorizationPoem(poemId)
        val totalCards = localDataSource.countCardsByPoemId(poemId)
        val lastReviewLog = localDataSource.getLastReviewLogByPoemId(poemId)

        return MemorizationPoem(
            poemId = poemId,
            title = detail.title,
            poetName = detail.poetName,
            categoryName = detail.categoryName,
            addedAtMillis = poem?.addedAtMillis ?: nowMillis,
            status = status,
            totalCards = totalCards,
            reviewedCards = lastReviewLog.sessionReviewed,
            reviewSessionsCount = lastReviewLog.reviewRound,
            nextReviewDays = poem?.interval ?: 0,
            dueDate = poem?.dueDate ?: 0L
        )
    }

    private fun notifySummaryChanged() {
        summaryRefresh.tryEmit(Unit)
        streakRefresh.tryEmit(Unit)
    }

    private fun syncReviewNotifications() {
        notificationScope.launch {
            reviewNotificationCoordinator.sync()
        }
    }

    private companion object {
        const val MAX_ACTIVE_POEMS = 3
        const val COMPLETION_THRESHOLD = 5
    }
}
