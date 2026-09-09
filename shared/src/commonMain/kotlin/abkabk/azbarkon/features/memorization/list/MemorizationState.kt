package abkabk.azbarkon.features.memorization.list

import abkabk.azbarkon.core.uidata.UiScreenState
import androidx.compose.runtime.Stable

@Stable
data class MemorizationPoemUi(
    val poemId: Int,
    val title: String,
    val poetName: String,
    val reviewCount: Int,
    val nextReviewDays: Int,
    val isCompleted: Boolean,
    val totalCards: Int = 0,
    val reviewedCards: Int = 0,
)

enum class MemorizationTab {
    ACTIVE,
    COMPLETED,
}

@Stable
data class MemorizationState(
    val screenState: UiScreenState = UiScreenState.Idle,
    val poems: List<MemorizationPoemUi> = emptyList(),
    val completedPoems: List<MemorizationPoemUi> = emptyList(),
    val selectedTab: MemorizationTab = MemorizationTab.ACTIVE,
    val poemToDelete: Int? = null,
)

sealed interface MemorizationAction {
    data object OnLoad : MemorizationAction

    data object OnResume : MemorizationAction

    data object OnBackClick : MemorizationAction

    data object OnAddPoemClick : MemorizationAction

    data class OnPoemClick(
        val poemId: Int,
    ) : MemorizationAction

    data class OnDeleteClick(
        val poemId: Int,
    ) : MemorizationAction

    data object OnDeleteConfirm : MemorizationAction

    data object OnDeleteDismiss : MemorizationAction

    data class OnTabSelected(
        val tab: MemorizationTab,
    ) : MemorizationAction

    data class OnReReviewClick(
        val poemId: Int,
    ) : MemorizationAction
}

sealed interface MemorizationEvent {
    data object NavigateBack : MemorizationEvent

    data object NavigateToSelect : MemorizationEvent

    data class NavigateToPractice(
        val poemId: Int,
    ) : MemorizationEvent
}
