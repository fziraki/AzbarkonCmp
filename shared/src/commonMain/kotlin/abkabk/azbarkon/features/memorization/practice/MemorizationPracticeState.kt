package abkabk.azbarkon.features.memorization.practice

import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.memorization.SrsGrade
import abkabk.azbarkon.domain.srs.DiffToken
import androidx.compose.runtime.Stable

@Stable
data class PracticeCardUi(
    val id: Long,
    val front: String,
    val back: String,
    val expectedContinuation: String,
    val poemTitle: String = "",
)

enum class PracticePhase {
    SHOW_FRONT,
    REVEALED,
    FEEDBACK,
    COMPLETE,
}

@Stable
data class MemorizationPracticeState(
    val screenState: UiScreenState = UiScreenState.Idle,
    val phase: PracticePhase = PracticePhase.SHOW_FRONT,
    val currentCard: PracticeCardUi? = null,
    val cardIndex: Int = 0,
    val totalCards: Int = 0,
    val isTypingMode: Boolean = false,
    val typedAnswer: String = "",
    val diffTokens: List<DiffToken> = emptyList(),
    val suggestedGrade: SrsGrade? = null,
    val selectedGrade: SrsGrade? = null,
    val gradesLocked: Boolean = false,
    val sessionMistakes: Int = 0,
    val sessionReviewed: Int = 0,
    val sessionLearned: Int = 0,
    val hasOtherDuePoems: Boolean = false,
    val nextPoemId: Int? = null,
)

sealed interface MemorizationPracticeAction {
    data object OnLoad : MemorizationPracticeAction

    data object OnBackClick : MemorizationPracticeAction

    data object OnRevealClick : MemorizationPracticeAction

    data object OnTypingModeClick : MemorizationPracticeAction

    data class OnTypedAnswerChange(
        val answer: String,
    ) : MemorizationPracticeAction

    data object OnSubmitTypedAnswer : MemorizationPracticeAction

    data class OnGradeClick(
        val grade: SrsGrade,
    ) : MemorizationPracticeAction

    data object OnNextCard : MemorizationPracticeAction

    data object OnNextPoemClick : MemorizationPracticeAction

    data object OnNotificationPermissionGranted : MemorizationPracticeAction
}

sealed interface MemorizationPracticeEvent {
    data object NavigateBack : MemorizationPracticeEvent

    data class NavigateToPoem(val poemId: Int) : MemorizationPracticeEvent
}
