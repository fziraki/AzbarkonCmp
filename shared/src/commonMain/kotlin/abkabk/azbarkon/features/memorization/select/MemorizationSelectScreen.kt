package abkabk.azbarkon.features.memorization.select

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.features.memorization.MemorizationHeroSection
import abkabk.azbarkon.features.memorization.MemorizationOptionRow
import abkabk.azbarkon.features.memorization.QuickStartCard
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.maktab
import sarv.shared.generated.resources.memorization_active_empty
import sarv.shared.generated.resources.memorization_active_poems
import sarv.shared.generated.resources.memorization_option_active_desc
import sarv.shared.generated.resources.memorization_option_library_desc
import sarv.shared.generated.resources.memorization_option_library_title
import sarv.shared.generated.resources.memorization_option_search_desc
import sarv.shared.generated.resources.memorization_option_search_title
import sarv.shared.generated.resources.new_memorization_title
import sarv.shared.generated.resources.review
import sarv.shared.generated.resources.search

@Composable
fun MemorizationSelectRoot(
    onBackClick: () -> Unit,
    onNavigateToPoetDetail: (Int) -> Unit,
    onNavigateToPoemList: (Int, String) -> Unit,
    onNavigateToTreasury: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToActivePoems: () -> Unit,
    viewModel: MemorizationSelectViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MemorizationSelectEvent.NavigateBack -> onBackClick()
            is MemorizationSelectEvent.NavigateToPoetDetail -> onNavigateToPoetDetail(event.poetId)
            is MemorizationSelectEvent.NavigateToPoemList ->
                onNavigateToPoemList(event.catId, event.title)
            MemorizationSelectEvent.NavigateToTreasury -> onNavigateToTreasury()
            MemorizationSelectEvent.NavigateToSearch -> onNavigateToSearch()
            MemorizationSelectEvent.NavigateToActivePoems -> onNavigateToActivePoems()
            is MemorizationSelectEvent.ShowSnackbar -> Unit
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        MemorizationSelectScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
fun MemorizationSelectScreen(
    state: MemorizationSelectState,
    onAction: (MemorizationSelectAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    Column(modifier = modifier.fillMaxSize()) {
        Header(
            title = stringResource(Res.string.new_memorization_title),
            onBackClick = { onAction(MemorizationSelectAction.OnBackClick) },
        )

        if (isExpanded) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(LocalSarvDimensions.current.dimen16),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            ) {
                MemorizationHeroSection()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
                ) {
                    QuickStartCard(
                        onCoupletClick = { onAction(MemorizationSelectAction.OnBabaTaherCoupletsClick) },
                        onGhazalClick = { onAction(MemorizationSelectAction.OnHafezGhazalsClick) },
                        onRubaiyatClick = { onAction(MemorizationSelectAction.OnKhayyamRubaiyatClick) },
                        modifier = Modifier.weight(1f),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
                    ) {
                        MemorizationOptionRow(
                            title = stringResource(Res.string.memorization_option_search_title),
                            description = stringResource(Res.string.memorization_option_search_desc),
                            icon = Res.drawable.search,
                            onClick = { onAction(MemorizationSelectAction.OnSearchClick) },
                        )

                        MemorizationOptionRow(
                            title = stringResource(Res.string.memorization_option_library_title),
                            description = stringResource(Res.string.memorization_option_library_desc),
                            icon = Res.drawable.maktab,
                            onClick = { onAction(MemorizationSelectAction.OnTreasuryClick) },
                        )

                        val activeDescription =
                            if (state.activePoemCount > 0) {
                                stringResource(
                                    Res.string.memorization_option_active_desc,
                                    state.activePoemCount,
                                )
                            } else {
                                stringResource(Res.string.memorization_active_empty)
                            }
                        MemorizationOptionRow(
                            title = stringResource(Res.string.memorization_active_poems),
                            description = activeDescription,
                            icon = Res.drawable.review,
                            onClick = { onAction(MemorizationSelectAction.OnActivePoemsClick) },
                        )
                    }
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(LocalSarvDimensions.current.dimen16),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            ) {
                MemorizationHeroSection()

                QuickStartCard(
                    onCoupletClick = { onAction(MemorizationSelectAction.OnBabaTaherCoupletsClick) },
                    onGhazalClick = { onAction(MemorizationSelectAction.OnHafezGhazalsClick) },
                    onRubaiyatClick = { onAction(MemorizationSelectAction.OnKhayyamRubaiyatClick) },
                )

                MemorizationOptionRow(
                    title = stringResource(Res.string.memorization_option_search_title),
                    description = stringResource(Res.string.memorization_option_search_desc),
                    icon = Res.drawable.search,
                    onClick = { onAction(MemorizationSelectAction.OnSearchClick) },
                )

                MemorizationOptionRow(
                    title = stringResource(Res.string.memorization_option_library_title),
                    description = stringResource(Res.string.memorization_option_library_desc),
                    icon = Res.drawable.maktab,
                    onClick = { onAction(MemorizationSelectAction.OnTreasuryClick) },
                )

                val activeDescription =
                    if (state.activePoemCount > 0) {
                        stringResource(
                            Res.string.memorization_option_active_desc,
                            state.activePoemCount,
                        )
                    } else {
                        stringResource(Res.string.memorization_active_empty)
                    }
                MemorizationOptionRow(
                    title = stringResource(Res.string.memorization_active_poems),
                    description = activeDescription,
                    icon = Res.drawable.review,
                    onClick = { onAction(MemorizationSelectAction.OnActivePoemsClick) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun MemorizationSelectScreenPreview() {
    SarvTheme {
        MemorizationSelectScreen(
            state =
                MemorizationSelectState(
                    activePoemCount = 2,
                ),
            onAction = {},
        )
    }
}
