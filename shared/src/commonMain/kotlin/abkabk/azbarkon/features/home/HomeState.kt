package abkabk.azbarkon.features.home

import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.Poet
import abkabk.azbarkon.domain.model.RandomDistich
import androidx.compose.runtime.Stable

enum class UpdateType {
    NONE,
    OPTIONAL,
    MANDATORY,
}

@Stable
data class MemorizationHeroUi(
    val hasActivePoems: Boolean = false,
    val activePoemCount: Int = 0,
    val dueCardsToday: Int = 0,
)

@Stable
data class HomeState(
    val screenState: UiScreenState = UiScreenState.Idle,
    val poets: List<Poet> = emptyList(),
    val memorizationHero: MemorizationHeroUi = MemorizationHeroUi(),
    val todayDistich: RandomDistich? = null,
    val updateType: UpdateType = UpdateType.NONE,
)

sealed interface HomeAction {
    data object OnLoad : HomeAction

    data object OnSeeAllPoetsClick : HomeAction

    data class OnPoetClick(
        val poetId: Int,
    ) : HomeAction

    data object OnMyPoemsClick : HomeAction

    data object OnSearchClick : HomeAction

    data object OnTasvirNegarClick : HomeAction

    data object OnMemorizationClick : HomeAction

    data object OnReviewClick : HomeAction

    data object OnChallengeClick : HomeAction

    data object OnDistichOfDayClick : HomeAction

    data object OnDismissUpdate : HomeAction
}

sealed interface HomeEvent {
    data object NavigateToPoetsList : HomeEvent

    data class NavigateToPoetDetail(
        val poetId: Int,
    ) : HomeEvent

    data class NavigateToPoemDetail(
        val poemId: Int,
    ) : HomeEvent

    data object NavigateToMyPoems : HomeEvent

    data object NavigateToSearch : HomeEvent

    data object NavigateToTasvirNegar : HomeEvent

    data object NavigateToMemorizationSelect : HomeEvent

    data object NavigateToMemorizationPractice : HomeEvent

    data object NavigateToActiveMemorization : HomeEvent

    data object NavigateToGame : HomeEvent
}
