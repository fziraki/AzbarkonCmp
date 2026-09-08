package abkabk.azbarkon.features.memorization.practice

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.domain.result.Result
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.model.memorization.SrsCard
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.srs.CardGenerator
import abkabk.azbarkon.domain.srs.TextDiffHighlighter
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.error_db_query
import sarv.shared.generated.resources.memorization_max_active_error
import sarv.shared.generated.resources.memorization_poem_not_error
import sarv.shared.generated.resources.memorization_card_not_error
import sarv.shared.generated.resources.memorization_review_notification_enabled
import kotlinx.coroutines.launch

class MemorizationPracticeViewModel(
    private val memorizationRepository: MemorizationRepository,
    private val poemId: Int?,
) : BaseViewModel<MemorizationPracticeAction, MemorizationPracticeState, MemorizationPracticeEvent>(
        initialState = MemorizationPracticeState(),
    ) {
    private var verseCards: List<SrsCard> = emptyList()
    private var currentQueueIndex = 0
    private val verseGrades = mutableListOf<SrsGrade>()
    private var consecutiveEasy = 0

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
        memorizationRepository
            .getDueCards(poemId)
            .onSuccess { cards ->
                verseCards = cards
                consecutiveEasy = cards.firstOrNull()?.consecutiveCorrect ?: 0
                verseGrades.clear()
                currentQueueIndex = 0

                if (cards.isEmpty()) {
                    val (hasOther, nextId) = findOtherDuePoem()
                    setState {
                        copy(
                            screenState = UiScreenState.Success,
                            phase = PracticePhase.COMPLETE,
                            currentCard = null,
                            totalCards = 0,
                            sessionMistakes = 0,
                            sessionReviewed = 0,
                            sessionLearned = 0,
                            hasOtherDuePoems = hasOther,
                            nextPoemId = nextId,
                        )
                    }
                } else {
                    setState {
                        copy(
                            sessionMistakes = 0,
                            sessionReviewed = 0,
                            sessionLearned = 0,
                        )
                    }
                    showCardAt(currentQueueIndex)
                }
            }.onFailure { error ->
                val message = error.toMemorizationUiText()
                setState { copy(screenState = UiScreenState.Error(message)) }
            }
    }

    private fun showCardAt(index: Int) {
        val card = verseCards.getOrNull(index)
        if (card == null) {
            viewModelScope.launch {
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
            }
            return
        }
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

        verseGrades.add(grade)
        recordSessionStats(grade)
        advanceToNextCard()
    }

    private fun recordSessionStats(grade: SrsGrade) {
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
    }

    private fun advanceToNextCard() {
        currentQueueIndex += 1
        if (currentQueueIndex >= verseCards.size) {
            submitPoemReview()
        } else {
            showCardAt(currentQueueIndex)
        }
    }

    private fun submitPoemReview() {
        if (poemId == null || verseGrades.isEmpty()) {
            viewModelScope.launch {
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
            }
            return
        }

        viewModelScope.launch {
            memorizationRepository
                .submitPoemReview(
                    poemId = poemId,
                    verseGrades = verseGrades.toList(),
                    consecutiveEasy = consecutiveEasy,
                )
                .onSuccess {
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
                }.onFailure {
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
                }
        }
    }

    private suspend fun findOtherDuePoem(): Pair<Boolean, Int?> {
        val activePoems = memorizationRepository.getActivePoems()
        return when (activePoems) {
            is abkabk.azbarkon.core.domain.result.Result.Success -> {
                val otherPoems = activePoems.data
                    .filter { it.poemId != poemId && it.dueCards > 0 }
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
