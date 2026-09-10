package abkabk.azbarkon.features.memorization.list

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.domain.model.memorization.MemorizationPoem
import abkabk.azbarkon.domain.model.memorization.MemorizationStatus
import abkabk.azbarkon.domain.repository.MemorizationRepository
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.error_db_query
import kotlinx.coroutines.launch

class MemorizationListViewModel(
    private val memorizationRepository: MemorizationRepository,
) : BaseViewModel<MemorizationAction, MemorizationState, MemorizationEvent>(
        initialState = MemorizationState(),
    ) {
    init {
        onAction(MemorizationAction.OnLoad)
    }

    override fun onAction(action: MemorizationAction) {
        when (action) {
            MemorizationAction.OnLoad -> loadPoems()

            MemorizationAction.OnResume -> loadPoems()

            MemorizationAction.OnBackClick -> {
                viewModelScope.launch { sendEvent(MemorizationEvent.NavigateBack) }
            }

            MemorizationAction.OnAddPoemClick -> {
                viewModelScope.launch { sendEvent(MemorizationEvent.NavigateToSelect) }
            }

            is MemorizationAction.OnPoemClick -> {
                viewModelScope.launch {
                    sendEvent(MemorizationEvent.NavigateToPractice(action.poemId))
                }
            }

            is MemorizationAction.OnDeleteClick -> {
                setState { copy(poemToDelete = action.poemId) }
            }

            MemorizationAction.OnDeleteDismiss -> {
                setState { copy(poemToDelete = null) }
            }

            MemorizationAction.OnDeleteConfirm -> {
                val poemId = state.value.poemToDelete ?: return
                viewModelScope.launch {
                    memorizationRepository
                        .removePoem(poemId)
                        .onSuccess {
                            setState { copy(poemToDelete = null) }
                            loadPoems()
                        }.onFailure {
                            setState {
                                copy(screenState = UiScreenState.Error(message = UiText.Resource(Res.string.error_db_query)))
                            }
                        }
                }
            }

            is MemorizationAction.OnTabSelected -> {
                setState { copy(selectedTab = action.tab) }
            }

            is MemorizationAction.OnReReviewClick -> {
                viewModelScope.launch {
                    memorizationRepository
                        .resetPoemToActive(action.poemId)
                        .onSuccess {
                            loadPoems()
                        }.onFailure {
                            setState {
                                copy(screenState = UiScreenState.Error(message = UiText.Resource(Res.string.error_db_query)))
                            }
                        }
                }
            }
        }
    }

    private fun loadPoems() {
        viewModelScope.launch {
            setState { copy(screenState = UiScreenState.Loading) }

            memorizationRepository
                .getPoemsByStatus(MemorizationStatus.ACTIVE)
                .onSuccess { poems ->
                    setState {
                        copy(
                            screenState = UiScreenState.Success,
                            poems = poems.map { it.toUi() },
                        )
                    }
                }.onFailure {
                    setState { copy(screenState = UiScreenState.Error(UiText.Resource(Res.string.error_db_query))) }
                }

            memorizationRepository
                .getPoemsByStatus(MemorizationStatus.COMPLETED)
                .onSuccess { poems ->
                    setState {
                        copy(
                            completedPoems = poems.map { it.toUi() },
                        )
                    }
                }
        }
    }
}

private fun MemorizationPoem.toUi(): MemorizationPoemUi =
    MemorizationPoemUi(
        poemId = poemId,
        title = title,
        poetName = poetName,
        reviewCount = reviewSessionsCount,
        nextReviewDays = nextReviewDays,
        isCompleted = status == MemorizationStatus.COMPLETED,
        totalCards = totalCards,
        reviewedCards = reviewedCards,
    )
