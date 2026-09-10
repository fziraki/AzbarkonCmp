package abkabk.azbarkon.features.memorization.practice

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.srs.CardGenerator
import abkabk.azbarkon.domain.srs.TextDiffHighlighter
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.error_db_query
import sarv.shared.generated.resources.memorization_card_not_error
import sarv.shared.generated.resources.memorization_max_active_error
import sarv.shared.generated.resources.memorization_poem_not_error
import sarv.shared.generated.resources.memorization_review_notification_enabled

class MemorizationPracticeViewModel(
    private val memorizationRepository: MemorizationRepository,
    private val poemId: Int?,
) : BaseViewModel<MemorizationPracticeAction, MemorizationPracticeState, MemorizationPracticeEvent>(
    initialState = MemorizationPracticeState(),
) {
    private var verseCards: List<SrsCard> = emptyList()
    private var currentQueueIndex = 0
    private var resolvedPoemId: Int = 0
    private var restoredMistakes = 0
    private var restoredReviewed = 0
    private var restoredLearned = 0

    init {
        onAction(MemorizationPracticeAction.OnLoad)
    }

    override fun onAction(action: MemorizationPracticeAction) {
        when (action) {
            MemorizationPracticeAction.OnLoad -> loadSession()

            MemorizationPracticeAction.OnBackClick -> {
                viewModelScope.launch { sendEvent(MemorizationPracticeEvent.NavigateBack) }
            }

            MemorizationPracticeAction.OnRevealClick -> revealCard()

            MemorizationPracticeAction.OnTypingModeClick -> enableTypingMode()

            is MemorizationPracticeAction.OnTypedAnswerChange -> {
                setState { copy(typedAnswer = action.answer) }
            }

            MemorizationPracticeAction.OnSubmitTypedAnswer -> evaluateTypedAnswer()

            is MemorizationPracticeAction.OnGradeClick -> selectGrade(action.grade)

            MemorizationPracticeAction.OnNextCard -> submitAndAdvance()

            MemorizationPracticeAction.OnNextPoemClick -> {
                val nextId = state.value.nextPoemId ?: return
                viewModelScope.launch { sendEvent(MemorizationPracticeEvent.NavigateToPoem(nextId)) }
            }

            MemorizationPracticeAction.OnNotificationPermissionGranted -> {
                setState {
                    copy(
                        screenState = UiScreenState.Error(
                            message = UiText.Resource(Res.string.memorization_review_notification_enabled),
                            isSuccess = true,
                        ),
                    )
                }
            }
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            setState { copy(screenState = UiScreenState.Loading) }
            loadDueCards()
        }
    }

    private suspend fun loadDueCards() {
        val (hasOther, nextId) = findOtherDuePoem()
        resolvedPoemId = poemId ?: nextId ?: return
        memorizationRepository
            .getDueCards(resolvedPoemId)
            .onSuccess { cards ->
                verseCards = cards
                currentQueueIndex = 0
                restoredMistakes = 0
                restoredReviewed = 0
                restoredLearned = 0

                if (cards.isNotEmpty()) {
                    val lastLog = memorizationRepository.getLastReviewLog(resolvedPoemId)
                    if (lastLog.sessionReviewed in 1 until cards.size) {
                        currentQueueIndex = lastLog.cardIndex + 1
                        restoredMistakes = lastLog.sessionMistakes
                        restoredReviewed = lastLog.sessionReviewed
                        restoredLearned = lastLog.sessionLearned
                    }
                }

            setState {
                copy(
                    screenState = UiScreenState.Success,
                    phase = if (cards.isEmpty()) PracticePhase.COMPLETE else PracticePhase.SHOW_FRONT,
                    hasOtherDuePoems = if (cards.isEmpty()) hasOther else false,
                    nextPoemId = if (cards.isEmpty()) nextId else null,
                    sessionMistakes = restoredMistakes,
                    sessionReviewed = restoredReviewed,
                    sessionLearned = restoredLearned,
                )
            }
                if (cards.isNotEmpty()) showCardAt(currentQueueIndex)
            }.onFailure { error ->
                val message = error.toMemorizationUiText()
                setState { copy(screenState = UiScreenState.Error(message)) }
            }
    }

    private fun showCardAt(index: Int) {
        val card = verseCards[index]
        setState {
            copy(
                screenState = UiScreenState.Success,
                phase = PracticePhase.SHOW_FRONT,
                currentCard =
                    PracticeCardUi(
                        id = card.id,
                        front = card.front,
                        back = card.back,
                        expectedContinuation =
                            CardGenerator.expectedContinuation(
                                front = card.front,
                                back = card.back,
                            ),
                    ),
                cardIndex = index + 1,
                totalCards = verseCards.size,
                isTypingMode = false,
                typedAnswer = "",
                diffTokens = emptyList(),
                suggestedGrade = null,
                selectedGrade = null,
                gradesLocked = false,
            )
        }
    }

    private fun revealCard() {
        if (state.value.phase != PracticePhase.SHOW_FRONT) return
        setState {
            copy(
                phase = PracticePhase.REVEALED,
                isTypingMode = false,
                typedAnswer = "",
                selectedGrade = null,
                suggestedGrade = null,
                gradesLocked = false,
            )
        }
    }

    private fun enableTypingMode() {
        if (state.value.phase != PracticePhase.SHOW_FRONT) return
        setState {
            copy(
                isTypingMode = true,
                typedAnswer = "",
                selectedGrade = null,
                suggestedGrade = null,
                gradesLocked = false,
            )
        }
    }

    private fun evaluateTypedAnswer() {
        val card = state.value.currentCard ?: return
        if (state.value.phase != PracticePhase.SHOW_FRONT || !state.value.isTypingMode) return
        if (state.value.typedAnswer.isBlank()) return

        val typedAnswer = state.value.typedAnswer.trim()
        val continuation = card.expectedContinuation
        val diff = TextDiffHighlighter.diffUserWords(continuation, typedAnswer)
        val suggested = TextDiffHighlighter.suggestGradeFromChars(continuation, typedAnswer)
        setState {
            copy(
                phase = PracticePhase.FEEDBACK,
                diffTokens = diff,
                suggestedGrade = suggested,
                selectedGrade = suggested,
                gradesLocked = true,
            )
        }
    }

    private fun selectGrade(grade: SrsGrade) {
        if (state.value.phase != PracticePhase.REVEALED || state.value.gradesLocked) return
        setState { copy(selectedGrade = grade) }
    }

    private fun submitAndAdvance() {
        val grade = state.value.selectedGrade ?: return

        when (state.value.phase) {
            PracticePhase.REVEALED,
            PracticePhase.FEEDBACK,
                -> Unit

            else -> return
        }

        viewModelScope.launch {
            submitReviewLog(grade)
            advanceToNextCard()
        }
    }

    private suspend fun submitReviewLog(grade: SrsGrade) {

        setState {
            copy(
                sessionReviewed = sessionReviewed + 1,
                sessionMistakes =
                    sessionMistakes +
                            when (grade) {
                                SrsGrade.AGAIN, SrsGrade.HARD -> 1
                                SrsGrade.GOOD, SrsGrade.EASY, SrsGrade.UNSPECIFIED -> 0
                            },
                sessionLearned =
                    sessionLearned +
                            when (grade) {
                                SrsGrade.GOOD, SrsGrade.EASY -> 1
                                SrsGrade.AGAIN, SrsGrade.HARD, SrsGrade.UNSPECIFIED -> 0
                            },
            )
        }

        val card = verseCards[currentQueueIndex]

        memorizationRepository.insertReviewLog(
            poemId = resolvedPoemId,
            cardId = card.id,
            grade = grade,
            minTotalScore = verseCards.size * 1.0,
            cardIndex = currentQueueIndex,
            sessionReviewed = state.value.sessionReviewed,
            sessionMistakes = state.value.sessionMistakes,
            sessionLearned = state.value.sessionLearned
        )
    }

    private suspend fun advanceToNextCard() {
        currentQueueIndex += 1
        if (currentQueueIndex >= verseCards.size) {

            memorizationRepository.submitPoemReview(
                poemId = resolvedPoemId
            )

            val (hasOther, nextId) = findOtherDuePoem()
            setState {
                copy(
                    screenState = UiScreenState.Success,
                    phase = PracticePhase.COMPLETE,
                    currentCard = null,
                    hasOtherDuePoems = hasOther,
                    nextPoemId = nextId,
                )
            }

        } else {
            showCardAt(currentQueueIndex)
        }
    }

    private suspend fun findOtherDuePoem(): Pair<Boolean, Int?> {
        val activePoems = memorizationRepository.getPoemsByStatus(MemorizationStatus.ACTIVE)
        return when (activePoems) {
            is abkabk.azbarkon.core.domain.result.Result.Success -> {
                val otherPoems = activePoems.data
                    .filter { it.poemId != resolvedPoemId && it.dueDate <= currentTimeMillis() }
                    .sortedBy { it.poemId }
                val first = otherPoems.firstOrNull()
                Pair(first != null, first?.poemId)
            }

            is abkabk.azbarkon.core.domain.result.Result.Error -> Pair(false, null)
        }
    }
}

private fun MemorizationError.toMemorizationUiText(): UiText =
    when (this) {
        MemorizationError.MaxActivePoemsReached ->
            UiText.Resource(Res.string.memorization_max_active_error)

        MemorizationError.PoemNotFound ->
            UiText.Resource(Res.string.memorization_poem_not_error)

        MemorizationError.CardNotFound ->
            UiText.Resource(Res.string.memorization_card_not_error)

        MemorizationError.Unknown ->
            UiText.Resource(Res.string.error_db_query)
    }
