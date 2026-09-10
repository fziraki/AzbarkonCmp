package abkabk.azbarkon.features.poets.list

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.core.uidata.toUiText
import abkabk.azbarkon.domain.model.PoetWithRootCategories
import abkabk.azbarkon.domain.repository.PoetDownloadError
import abkabk.azbarkon.domain.repository.PoetDownloadRepository
import abkabk.azbarkon.domain.repository.PoetRepository
import abkabk.azbarkon.features.poets.FeaturedPoetUi
import abkabk.azbarkon.features.poets.toFeaturedPoetUi
import abkabk.azbarkon.features.poets.toListItemUi
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.poets_download_merge_error
import sarv.shared.generated.resources.poets_download_network_error
import sarv.shared.generated.resources.poets_download_success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class PoetsListViewModel(
    private val poetRepository: PoetRepository,
    private val poetDownloadRepository: PoetDownloadRepository,
    private val random: Random = Random.Default,
) : BaseViewModel<PoetsListAction, PoetsListState, PoetsListEvent>(
        initialState = PoetsListState(),
    ) {
    private var allPoetsWithRootCategories: List<PoetWithRootCategories> = emptyList()
    private var featuredPoetWhenIdle: FeaturedPoetUi? = null

    init {
        onAction(PoetsListAction.OnLoad)
    }

    override fun onAction(action: PoetsListAction) {
        when (action) {
            PoetsListAction.OnLoad -> loadPoets()

            PoetsListAction.OnScreenEnter -> pickAndApplyFeaturedPoet()

            is PoetsListAction.OnSearchQueryChange -> updateSearch(action.query)

            is PoetsListAction.OnDownloadPoet -> downloadPoet(action.poetId)

            is PoetsListAction.OnPoetClick -> {
                viewModelScope.launch {
                    sendEvent(PoetsListEvent.NavigateToPoetDetail(action.poetId))
                }
            }

            PoetsListAction.OnFeaturedPoetClick -> {
                val featuredId = state.value.featuredPoet?.id ?: return
                viewModelScope.launch {
                    sendEvent(PoetsListEvent.NavigateToPoetDetail(featuredId))
                }
            }

            is PoetsListAction.OnChatClick -> {
                viewModelScope.launch {
                    sendEvent(PoetsListEvent.NavigateToChat(action.poetId))
                }
            }
        }
    }

    private fun loadPoets() {
        viewModelScope.launch {
            setState { copy(screenState = UiScreenState.Loading) }

            poetRepository.getPoetsWithRootCategories()
                .onSuccess { poetsWithRootCategories ->
                    allPoetsWithRootCategories = poetsWithRootCategories
                    val featured = pickRandomFeatured(featuredCandidates(poetsWithRootCategories))
                    applyFilter(
                        query = state.value.searchQuery,
                        source = poetsWithRootCategories,
                        featuredPoet = featured,
                    )
                }.onFailure { error ->
                    val message = error.toUiText()
                    setState {
                        copy(
                            screenState = UiScreenState.Error(message = message),
                        )
                    }
                }
        }
    }

    private fun pickAndApplyFeaturedPoet() {
        if (allPoetsWithRootCategories.isEmpty()) return
        if (state.value.searchQuery.trim().isNotEmpty()) return

        val featured = pickRandomFeatured(featuredCandidates(allPoetsWithRootCategories))
        applyFilter(
            query = state.value.searchQuery,
            source = allPoetsWithRootCategories,
            featuredPoet = featured,
        )
    }

    private fun updateSearch(query: String) {
        setState { copy(searchQuery = query) }
        applyFilter(
            query = query,
            source = allPoetsWithRootCategories,
        )
    }

    private fun applyFilter(
        query: String,
        source: List<PoetWithRootCategories>,
        featuredPoet: FeaturedPoetUi? = null,
    ) {
        val trimmedQuery = query.trim()
        val filtered =
            if (trimmedQuery.isEmpty()) {
                source
            } else {
                source.filter { item ->
                    item.poet.name?.contains(trimmedQuery) == true ||
                        item.rootCategories.any { category -> category.text.contains(trimmedQuery) }
                }
            }

        val listItems = filtered.map { it.toListItemUi() }
        val resolvedFeatured =
            when {
                trimmedQuery.isNotEmpty() -> null
                featuredPoet != null -> featuredPoet.also { featuredPoetWhenIdle = it }
                else -> featuredPoetWhenIdle
            }

        setState {
            copy(
                screenState = UiScreenState.Success,
                poets = listItems,
                featuredPoet = resolvedFeatured,
            )
        }
    }

    private fun featuredCandidates(source: List<PoetWithRootCategories>): List<PoetWithRootCategories> =
        source.filter { it.poet.isDownloaded }

    private fun pickRandomFeatured(source: List<PoetWithRootCategories>): FeaturedPoetUi? =
        source.randomOrNull(random)?.toFeaturedPoetUi()

    private fun downloadPoet(poetId: Int) {
        if (poetId in state.value.downloadingPoetIds) return
        viewModelScope.launch {
            setState { copy(downloadingPoetIds = downloadingPoetIds + poetId) }
            poetDownloadRepository.downloadPoet(poetId)
                .onSuccess {
                    setState {
                        copy(
                            downloadingPoetIds = downloadingPoetIds - poetId,
                            screenState = UiScreenState.Error(
                                message = UiText.Resource(Res.string.poets_download_success),
                                isSuccess = true
                            )
                        )
                    }
                    delay(1000.milliseconds)
                    loadPoets()
                }
                .onFailure { error ->
                    val message = when (error) {
                        PoetDownloadError.Network -> UiText.Resource(Res.string.poets_download_network_error)
                        PoetDownloadError.MergeFailed -> UiText.Resource(Res.string.poets_download_merge_error)
                    }
                    setState {
                        copy(
                            downloadingPoetIds = downloadingPoetIds - poetId,
                            screenState = UiScreenState.Error(
                                message = message,
                            )
                        )
                    }
                }
        }
    }
}
