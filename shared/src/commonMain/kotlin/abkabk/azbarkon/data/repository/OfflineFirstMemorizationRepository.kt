package abkabk.azbarkon.data.repository

import abkabk.azbarkon.core.domain.result.EmptyResult
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.domain.datasource.MemorizationLocalDataSource
import abkabk.azbarkon.domain.memorization.MemorizationReviewNotificationCoordinator
import abkabk.azbarkon.domain.model.memorization.ActiveMemorizationPoem
import abkabk.azbarkon.domain.model.memorization.ActiveMemorizationStatus
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationSummary
import abkabk.azbarkon.domain.model.memorization.QuickStartTarget
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.repository.PoemRepository
import abkabk.azbarkon.domain.srs.CardGenerator
import abkabk.azbarkon.domain.srs.SrsScheduler
import abkabk.azbarkon.core.util.consecutiveDayStreak
import abkabk.azbarkon.core.util.currentTimeMillis
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

    override suspend fun getActivePoems(): Result<List<ActiveMemorizationPoem>, MemorizationError> =
        try {
            val now = currentTimeMillis()
            val poemIds = localDataSource.getActivePoemIds()
            val poems =
                poemIds.mapNotNull { poemId ->
                    buildActivePoem(poemId, now)
                }
            Result.Success(poems)
        } catch (e: IllegalStateException) {
            Napier.e("getActivePoems failed", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun getCompletedPoems(): Result<List<ActiveMemorizationPoem>, MemorizationError> =
        try {
            val now = currentTimeMillis()
            val poemIds = localDataSource.getActivePoemIdsByStatus(ActiveMemorizationStatus.COMPLETED.name)
            val poems =
                poemIds.mapNotNull { poemId ->
                    buildActivePoem(poemId, now)
                }
            Result.Success(poems)
        } catch (e: IllegalStateException) {
            Napier.e("getCompletedPoems failed", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun resetPoemToActive(poemId: Int): EmptyResult<MemorizationError> =
        try {
            localDataSource.updatePoemStatus(poemId, ActiveMemorizationStatus.ACTIVE.name)
            notifySummaryChanged()
            syncReviewNotifications()
            Result.Success(Unit)
        } catch (e: IllegalStateException) {
            Napier.e("resetPoemToActive failed for poemId=$poemId", e)
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
                val cards = CardGenerator.generateCards(poemId, detailResult.data.verses.filter { it.position >= 0 }, now)
                if (cards.isEmpty()) {
                    Result.Error(MemorizationError.PoemNotFound)
                } else {
                    localDataSource.insertActivePoem(
                        poemId = poemId,
                        addedAtMillis = now,
                        status = ActiveMemorizationStatus.ACTIVE.name,
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
            localDataSource.deleteActivePoem(poemId)
            notifySummaryChanged()
            syncReviewNotifications()
            Result.Success(Unit)
        } catch (e: IllegalStateException) {
            Napier.e("removePoem failed for poemId=$poemId", e)
            Result.Error(MemorizationError.Unknown)
        }

    override suspend fun getDueCards(poemId: Int?): Result<List<SrsCard>, MemorizationError> =
        try {
            val cards = localDataSource.getDueCards(currentTimeMillis(), poemId)
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

    override suspend fun submitReview(
        cardId: Long,
        grade: SrsGrade,
    ): Result<SrsCard, MemorizationError> {
        val card = localDataSource.getCardById(cardId) ?: return Result.Error(MemorizationError.CardNotFound)
        val now = currentTimeMillis()
        val previousInterval = card.interval
        val newScore = SrsScheduler.updateVerseScore(card.score, grade)
        val result = SrsScheduler.calculatePoemInterval(listOf(newScore), card.consecutiveCorrect)
        val updated =
            card.copy(
                interval = result.interval,
                score = newScore,
                dueDateMillis = result.dueDateMillis,
                consecutiveCorrect = result.consecutiveEasy,
            )
        return try {
            localDataSource.updateCard(updated)
            localDataSource.insertReviewLog(
                cardId = cardId,
                grade = grade,
                previousInterval = previousInterval,
                newInterval = result.interval,
                reviewTimeMillis = now,
            )
            notifySummaryChanged()
            syncReviewNotifications()
            Result.Success(updated)
        } catch (e: IllegalStateException) {
            Napier.e("submitReview failed for cardId=$cardId", e)
            Result.Error(MemorizationError.Unknown)
        }
    }

    override suspend fun submitPoemReview(
        poemId: Int,
        verseGrades: List<SrsGrade>,
        consecutiveEasy: Int,
    ): Result<Int, MemorizationError> {
        val cards = localDataSource.getCardsByPoemId(poemId)
        if (cards.isEmpty()) return Result.Error(MemorizationError.CardNotFound)

        val now = currentTimeMillis()
        val reviewedCards = cards.mapIndexedNotNull { index, card ->
            val grade = verseGrades.getOrNull(index)
            if (grade != null && grade != SrsGrade.UNSPECIFIED) card to grade else null
        }

        if (reviewedCards.isEmpty()) {
            return Result.Success(0)
        }

        val verseScores = reviewedCards.map { (card, grade) ->
            SrsScheduler.updateVerseScore(card.score, grade)
        }

        val result = SrsScheduler.calculatePoemInterval(verseScores, consecutiveEasy)

        return try {
            reviewedCards.forEach { (card, grade) ->
                val newScore = SrsScheduler.updateVerseScore(card.score, grade)
                localDataSource.updateCard(
                    card.copy(
                        interval = result.interval,
                        dueDateMillis = result.dueDateMillis,
                        score = newScore,
                        consecutiveCorrect = result.consecutiveEasy,
                    ),
                )
                localDataSource.insertReviewLog(
                    cardId = card.id,
                    grade = grade,
                    previousInterval = card.interval,
                    newInterval = result.interval,
                    reviewTimeMillis = now,
                )
            }
            val reviewedIds = reviewedCards.map { it.first.id }.toSet()
            cards.filter { it.id !in reviewedIds }
                .forEach { card ->
                    localDataSource.updateCard(
                        card.copy(consecutiveCorrect = result.consecutiveEasy),
                    )
                }
            notifySummaryChanged()
            syncReviewNotifications()
            Result.Success(result.interval)
        } catch (e: IllegalStateException) {
            Napier.e("submitPoemReview failed for poemId=$poemId", e)
            Result.Error(MemorizationError.Unknown)
        }
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
        val now = currentTimeMillis()
        return MemorizationSummary(
            activePoemCount = localDataSource.countActivePoems(),
            dueCardsToday = localDataSource.countDueCards(now),
        )
    }

    private suspend fun loadPracticeStreak(): Int {
        val dayKeys = localDataSource.getReviewDayKeys()
        return consecutiveDayStreak(dayKeys)
    }

    private suspend fun buildActivePoem(
        poemId: Int,
        nowMillis: Long,
    ): ActiveMemorizationPoem? {
        val detail =
            poemRepository.getPoemDetail(poemId).let { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> return null
                }
            }
        val totalCards = localDataSource.countCardsByPoemId(poemId)
        val reviewedCards = localDataSource.countReviewedCardsByPoemId(poemId)
        val dueCards = localDataSource.countDueCards(nowMillis, poemId)
        val addedAt = localDataSource.getActivePoemAddedAt(poemId) ?: nowMillis
        val maxInterval = localDataSource.getMaxIntervalByPoemId(poemId)
        val reviewCount = localDataSource.getReviewCountByPoemId(poemId)

        val status = when {
            localDataSource.getActivePoemIdsByStatus(ActiveMemorizationStatus.COMPLETED.name).contains(poemId) ->
                ActiveMemorizationStatus.COMPLETED
            localDataSource.getActivePoemIdsByStatus(ActiveMemorizationStatus.PAUSED.name).contains(poemId) ->
                ActiveMemorizationStatus.PAUSED
            else -> ActiveMemorizationStatus.ACTIVE
        }

        return ActiveMemorizationPoem(
            poemId = poemId,
            title = detail.title,
            poetName = detail.poetName,
            categoryName = detail.categoryName,
            addedAtMillis = addedAt,
            status = status,
            totalCards = totalCards,
            reviewedCards = reviewedCards,
            dueCards = dueCards,
            reviewCount = reviewCount,
            nextReviewDays = maxInterval,
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
    }
}
