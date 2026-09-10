package abkabk.azbarkon.features.poems.list

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.components.HeaderAction
import abkabk.azbarkon.ui.components.ShimmerPlaceholder
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.list_load_error
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid

@Composable
fun PoemListRoot(
    catId: Int,
    title: String,
    onBackClick: () -> Unit,
    onNavigateToPoemDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: PoemListViewModel = koinViewModel { parametersOf(catId, title) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val poems = viewModel.poems.collectAsLazyPagingItems()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PoemListEvent.NavigateToPoemDetail -> onNavigateToPoemDetail(event.poemId)
        }
    }

    val screenState =
        remember(poems.loadState.refresh) {
            when {
                poems.loadState.refresh is LoadState.Loading && poems.itemCount == 0 ->
                    UiScreenState.Loading
                poems.loadState.refresh is LoadState.Error ->
                    UiScreenState.Error(
                        message = UiText.Resource(Res.string.list_load_error),
                    )
                else -> UiScreenState.Success
            }
        }

    BaseScreen(
        screenState = screenState,
    ) {
        PoemListScreen(
            state = state,
            poems = poems,
            onBackClick = onBackClick,
            onPoemClick = { poemId -> viewModel.onAction(PoemListAction.OnPoemClick(poemId)) },
            onSearchClick = onNavigateToSearch,
        )
    }
}

@Composable
fun PoemListScreen(
    state: PoemListState,
    poems: LazyPagingItems<PoemListItemUi>,
    onBackClick: () -> Unit,
    onPoemClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Header(
            title = state.title,
            onBackClick = onBackClick,
            action = HeaderAction.Search(onSearchClick),
        )

        val columns =
            when (LocalWindowSizeClass.current.widthSizeClass) {
                WindowWidthSizeClass.Expanded -> GridCells.Fixed(2)
                else -> GridCells.Fixed(1)
            }

        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = LocalSarvDimensions.current.dimen16,
                vertical = LocalSarvDimensions.current.dimen24,
            ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            items(
                count = poems.itemCount,
                key = poems.itemKey { poem -> poem.id },
            ) { index ->
                poems[index]?.let { poem ->
                    Text(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = LocalSarvDimensions.current.dimen1,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                                ).clickable { onPoemClick(poem.id) }
                                .padding(LocalSarvDimensions.current.dimen16),
                        text = poem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                    )
                }
            }

            if (poems.loadState.append is LoadState.Loading) {
                item {
                    ShimmerPlaceholder(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(LocalSarvDimensions.current.dimen56)
                                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PoemListScreenPreview() {
    SarvTheme {
        PoemListScreen(
            state = PoemListState(title = "غزلیات"),
            poems =
                flowOf(
                    PagingData.from(
                        listOf(
                            PoemListItemUi(id = 1, title = "شمارهٔ ۱"),
                            PoemListItemUi(id = 2, title = "شمارهٔ ۲"),
                        ),
                    ),
                ).collectAsLazyPagingItems(),
            onBackClick = {},
            onPoemClick = {},
            onSearchClick = {},
        )
    }
}
