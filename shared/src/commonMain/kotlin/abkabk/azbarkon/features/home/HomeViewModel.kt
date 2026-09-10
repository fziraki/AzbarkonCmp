package abkabk.azbarkon.features.home

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.toUiText
import abkabk.azbarkon.domain.repository.DailyDistichRepository
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.repository.PoetRepository
import abkabk.azbarkon.domain.platform.AppUpdateRepository
import abkabk.azbarkon.features.profile.util.platformName
import abkabk.azbarkon.features.profile.util.versionCode
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val poetRepository: PoetRepository,
    private val memorizationRepository: MemorizationRepository,
    private val dailyDistichRepository: DailyDistichRepository,
    private val appUpdateRepository: AppUpdateRepository,
) : BaseViewModel<HomeAction, HomeState, HomeEvent>(
        initialState = HomeState(),
    ) {
    init {
        onAction(HomeAction.OnLoad)
        observeMemorizationSummary()
        loadTodayDistich()
        checkForUpdate()
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnLoad -> loadPoets()

            HomeAction.OnSeeAllPoetsClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToPoetsList)
                }
            }

            is HomeAction.OnPoetClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToPoetDetail(action.poetId))
                }
            }

            HomeAction.OnMyPoemsClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToMyPoems)
                }
            }

            HomeAction.OnSearchClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToSearch)
                }
            }

            HomeAction.OnTasvirNegarClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToTasvirNegar)
                }
            }

            HomeAction.OnMemorizationClick -> {
                viewModelScope.launch {
                    val hero = state.value.memorizationHero
                    if (hero.hasActivePoems) {
                        if (hero.dueCardsToday > 0) {
                            sendEvent(HomeEvent.NavigateToMemorizationPractice)
                        } else {
                            sendEvent(HomeEvent.NavigateToActiveMemorization)
                        }
                    } else {
                        sendEvent(HomeEvent.NavigateToMemorizationSelect)
                    }
                }
            }

            HomeAction.OnReviewClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToMemorizationSelect)
                }
            }

            HomeAction.OnChallengeClick -> {
                viewModelScope.launch {
                    sendEvent(HomeEvent.NavigateToGame)
                }
            }

            HomeAction.OnDistichOfDayClick -> {
                viewModelScope.launch {
                    state.value.todayDistich?.poemId?.let { poemId ->
                        sendEvent(HomeEvent.NavigateToPoemDetail(poemId))
                    }
                }
            }

            HomeAction.OnDismissUpdate -> {
                setState { copy(updateType = UpdateType.NONE) }
            }
        }
    }

    private fun observeMemorizationSummary() {
        memorizationRepository
            .observeActiveSummary()
            .onEach { summary ->
                setState {
                    copy(
                        memorizationHero =
                            MemorizationHeroUi(
                                hasActivePoems = summary.activePoemCount > 0,
                                activePoemCount = summary.activePoemCount,
                                dueCardsToday = summary.dueCardsToday,
                            ),
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun loadTodayDistich() {
        viewModelScope.launch {
            dailyDistichRepository.getTodayDistich()
                .onSuccess { distich ->
                    setState { copy(todayDistich = distich) }
                }
                .onFailure {
                    Napier.e("Failed to load today's distich")
                }
        }
    }

    private fun loadPoets() {
        viewModelScope.launch {
            Napier.d("Home: loading poets, current screenState=${state.value.screenState}")
            setState {
                copy(screenState = UiScreenState.Loading)
            }

            poetRepository.getPoets()
                .onSuccess { poets ->
                    Napier.d(
                        message = "Loaded ${poets.size} poets, downloaded=${poets.count { it.isDownloaded }}",
                        tag = "Home",
                    )
                    setState {
                        copy(
                            screenState = UiScreenState.Success,
                            poets = poets.filter { it.isDownloaded },
                        )
                    }
                }.onFailure { error ->
                    Napier.e("Failed to load poets: $error")
                    val message = error.toUiText()
                    setState {
                        copy(
                            screenState =
                                UiScreenState.Error(
                                    message = message,
                                ),
                        )
                    }
                }
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            Napier.d("UpdateCheck: starting...")
            appUpdateRepository.getUpdateConfig()
                .onSuccess { config ->
                    val currentVersion = versionCode()
                    val platform = platformName()
                    val platformConfig = if (platform == "android") {
                        config.android
                    } else {
                        config.ios
                    }

                    Napier.d(
                        "UpdateCheck: platform=$platform, " +
                            "currentVersion=$currentVersion, " +
                            "stable=${platformConfig.stable_version_code}, " +
                            "last=${platformConfig.last_version_code}",
                    )

                    val updateType = when {
                        currentVersion < platformConfig.stable_version_code -> UpdateType.MANDATORY
                        currentVersion < platformConfig.last_version_code -> UpdateType.OPTIONAL
                        else -> UpdateType.NONE
                    }

                    Napier.d("UpdateCheck: updateType=$updateType")
                    setState { copy(updateType = updateType) }
                }
                .onFailure { error ->
                    Napier.e("UpdateCheck: failed - $error")
                }
        }
    }
}
