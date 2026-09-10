package abkabk.azbarkon.features.games.session

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSnackbarHostState
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.core.uidata.asString
import abkabk.azbarkon.domain.model.games.GameQuestion
import abkabk.azbarkon.domain.model.games.GameSessionSummary
import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.features.games.completepoem.CompletePoemContent
import abkabk.azbarkon.features.games.components.GameContentShimmer
import abkabk.azbarkon.features.games.components.GameSessionBottomBar
import abkabk.azbarkon.features.games.components.GameSessionTopBar
import abkabk.azbarkon.features.games.findpoet.FindPoetContent
import abkabk.azbarkon.features.games.navigation.GameTypeRoute
import abkabk.azbarkon.features.games.navigation.toDomain
import abkabk.azbarkon.features.games.nextverse.NextVerseContent
import abkabk.azbarkon.features.games.organizepoem.OrganizePoemContent
import abkabk.azbarkon.ui.components.SarvSnackbarHost
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun GameSessionRoot(
    gameTypeRoute: GameTypeRoute,
    onBackClick: () -> Unit,
    onNavigateToResult: (GameType, GameSessionSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GameSessionViewModel =
        koinViewModel {
            parametersOf(gameTypeRoute.toDomain())
        }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    var snackbarMessage by remember { mutableStateOf<UiText?>(null) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GameSessionEvent.NavigateBack -> onBackClick()
            is GameSessionEvent.NavigateToResult ->
                onNavigateToResult(event.gameType, event.summary)
            is GameSessionEvent.ShowSnackbar -> {
                snackbarMessage = event.message
            }
        }
    }

    val resolvedSnackbarMessage = snackbarMessage?.asString()
    LaunchedEffect(resolvedSnackbarMessage) {
        if (resolvedSnackbarMessage != null) {
            snackbarHostState.showSnackbar(
                message = resolvedSnackbarMessage,
                duration = SnackbarDuration.Short,
            )
            snackbarMessage = null
        }
    }

    BaseScreen(
        screenState = state.screenState,
        modifier = modifier,
    ) {
        GameSessionScreen(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun GameSessionScreen(
    state: GameSessionState,
    onAction: (GameSessionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            GameSessionTopBar(
                gameType = state.gameType,
                coinBalance = state.coinBalance,
                currentQuizIndex = state.currentQuizIndex,
                onBackClick = { onAction(GameSessionAction.OnBackClick) },
            )
        },
        bottomBar = {
            GameSessionBottomBar(
                hasSelection = state.hasSelection,
                isRevealing = state.isRevealing,
                canPressPrimaryAction = state.canPressPrimaryAction,
                onHintClick = { onAction(GameSessionAction.OnHintClick) },
                onCheckAnswerClick = { onAction(GameSessionAction.OnCheckAnswerClick) },
            )
        },
        snackbarHost = {
            SarvSnackbarHost(hostState = LocalSnackbarHostState.current)
        },
    ) { paddingValues ->
        val needsScroll = state.gameType != GameType.ORGANIZE_POEM
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (needsScroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(
                        top = paddingValues.calculateTopPadding() + LocalSarvDimensions.current.dimen16,
                        bottom = paddingValues.calculateBottomPadding(),
                        start = LocalSarvDimensions.current.dimen16,
                        end = LocalSarvDimensions.current.dimen16
                        ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            when (val question = state.currentQuestion) {
                is GameQuestion.NextVerse ->
                    NextVerseContent(
                        question = question,
                        selectedOptionIndex = state.selectedOptionIndex,
                        disabledOptionIndices = state.disabledOptionIndices,
                        answerPhase = state.answerPhase,
                        enabled = state.isAnswering,
                        onOptionSelect = { onAction(GameSessionAction.OnOptionSelected(it)) },
                        modifier = Modifier.fillMaxSize(),
                    )

                is GameQuestion.FindPoet ->
                    FindPoetContent(
                        question = question,
                        selectedPoetId = state.selectedPoetId,
                        disabledOptionIndices = state.disabledOptionIndices,
                        answerPhase = state.answerPhase,
                        enabled = state.isAnswering,
                        onPoetSelect = { onAction(GameSessionAction.OnPoetSelected(it)) },
                        modifier = Modifier.fillMaxSize(),
                    )

                is GameQuestion.CompletePoem ->
                    CompletePoemContent(
                        question = question,
                        filledWords = state.filledWords,
                        disabledOptionIndices = state.disabledOptionIndices,
                        answerPhase = state.answerPhase,
                        enabled = state.isAnswering,
                        onWordSelect = { onAction(GameSessionAction.OnWordSelected(it)) },
                        modifier = Modifier.fillMaxSize(),
                    )

                is GameQuestion.OrganizePoem ->
                    OrganizePoemContent(
                        question = question,
                        orderedLineIds = state.orderedLineIds,
                        initialOrderedLineIds = state.initialOrderedLineIds,
                        pinnedLineId = state.pinnedLineId,
                        answerPhase = state.answerPhase,
                        enabled = state.isAnswering,
                        onReorder = { from, to ->
                            onAction(GameSessionAction.OnReorderLines(from, to))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                null ->
                    if (state.screenState == UiScreenState.Loading) {
                        GameContentShimmer(
                            gameType = state.gameType,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
            }
        }
    }
}
