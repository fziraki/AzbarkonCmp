package abkabk.azbarkon.features.search

import abkabk.azbarkon.core.ui.HighlightedText
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.ui.components.SarvModalBottomSheet
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.components.ShimmerPlaceholder
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.all
import sarv.shared.generated.resources.list_load_error
import sarv.shared.generated.resources.search
import sarv.shared.generated.resources.search_choose_category
import sarv.shared.generated.resources.search_choose_poet
import sarv.shared.generated.resources.search_no_results
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass

private const val SHIMMER_ROW_COUNT = 7
@Composable
fun SearchRoot(
    initialPoetId: Int?,
    initialCatId: Int?,
    onBackClick: () -> Unit,
    onNavigateToPoemDetail: (Int) -> Unit,
    viewModelArgs: SearchNavigationArgs = SearchNavigationArgs(initialPoetId, initialCatId),
    viewModel: SearchViewModel = koinViewModel { parametersOf(viewModelArgs) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SearchEvent.NavigateToPoemDetail -> onNavigateToPoemDetail(event.poemId)
        }
    }

    val screenState =
        remember(
            state.isInitializing,
            state.submittedQuery,
            state.screenState,
            searchResults.loadState.refresh,
        ) {
            when {
                state.screenState is UiScreenState.Error -> state.screenState
                state.isInitializing -> UiScreenState.Loading
                searchResults.loadState.refresh is LoadState.Error && state.submittedQuery.isNotBlank() ->
                    UiScreenState.Error(
                        message = UiText.Resource(Res.string.list_load_error),
                    )
                else -> UiScreenState.Success
            }
        }

    BaseScreen(
        screenState = screenState,
    ) {
        SearchScreen(
            state = state,
            searchResults = searchResults,
            isSearching = state.isSearching,
            onAction = viewModel::onAction,
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun SearchScreen(
    state: SearchState,
    searchResults: LazyPagingItems<SearchResultUi>,
    onAction: (SearchAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var showPoetPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val allLabel = stringResource(Res.string.all)

    val selectedPoetLabel =
        state.poetOptions
            .firstOrNull { it.id == state.selectedPoetId }
            ?.let { option -> if (option.id == null) allLabel else option.name }
            ?: allLabel

    val selectedCategoryLabel =
        state.categoryOptions
            .firstOrNull { it.id == state.selectedCategoryId }
            ?.let { option -> if (option.id == null) allLabel else option.title }
            ?: allLabel

    val showNoResults =
        searchResults.loadState.refresh is LoadState.NotLoading &&
            searchResults.itemCount == 0 &&
            state.submittedQuery.isNotBlank()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Header(
            title = stringResource(Res.string.search),
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
        ) {
            SearchQueryField(
                value = state.query,
                onValueChange = { onAction(SearchAction.OnQueryChange(it)) },
                onSearch = {
                    keyboardController?.hide()
                    onAction(SearchAction.OnSearchSubmit)
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
            ) {
                SearchPickerField(
                    label = stringResource(Res.string.search_choose_poet),
                    value = selectedPoetLabel,
                    onClick = { showPoetPicker = true },
                    modifier = Modifier.weight(1f),
                )
                SearchPickerField(
                    label = stringResource(Res.string.search_choose_category),
                    value = selectedCategoryLabel,
                    onClick = { if (state.isCategoryPickerEnabled) showCategoryPicker = true },
                    enabled = state.isCategoryPickerEnabled,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SearchResultsList(
            searchResults = searchResults,
            submittedQuery = state.submittedQuery,
            isSearching = isSearching,
            showNoResults = showNoResults,
            onResultClick = { poemId -> onAction(SearchAction.OnResultClick(poemId)) },
        )
    }

    SearchOptionSheets(
        state = state,
        allLabel = allLabel,
        showPoetPicker = showPoetPicker,
        showCategoryPicker = showCategoryPicker,
        onDismissPoetPicker = { showPoetPicker = false },
        onDismissCategoryPicker = { showCategoryPicker = false },
        onSelectPoet = { optionId -> onAction(SearchAction.OnPoetSelected(optionId)) },
        onSelectCategory = { optionId -> onAction(SearchAction.OnCategorySelected(optionId)) },
    )
}

@Composable
private fun SearchResultsList(
    searchResults: LazyPagingItems<SearchResultUi>,
    submittedQuery: String,
    isSearching: Boolean,
    showNoResults: Boolean,
    onResultClick: (Int) -> Unit,
) {
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val columns = if (isExpanded) GridCells.Fixed(2) else GridCells.Fixed(1)

    when {
        isSearching -> SearchResultsShimmer()

        showNoResults -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.search_no_results),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = columns,
                state = rememberLazyGridState(),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = LocalSarvDimensions.current.dimen16,
                    vertical = LocalSarvDimensions.current.dimen8,
                ),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
            ) {
                items(
                    count = searchResults.itemCount,
                    key = searchResults.itemKey { result -> result.key },
                ) { index ->
                    searchResults[index]?.let { result ->
                        SearchResultRow(
                            result = result,
                            submittedQuery = submittedQuery,
                            onClick = { onResultClick(result.poemId) },
                        )
                    }
                }

                if (searchResults.loadState.append is LoadState.Loading) {
                    item {
                        ShimmerPlaceholder(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(LocalSarvDimensions.current.dimen64)
                                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsShimmer() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen8),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
    ) {
        repeat(SHIMMER_ROW_COUNT) {
            ShimmerPlaceholder(
                modifier =
                    Modifier.fillMaxWidth().height(LocalSarvDimensions.current.dimen64)
                        .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16)),
            )
        }
    }
}

@Composable
private fun SearchOptionSheets(
    state: SearchState,
    allLabel: String,
    showPoetPicker: Boolean,
    showCategoryPicker: Boolean,
    onDismissPoetPicker: () -> Unit,
    onDismissCategoryPicker: () -> Unit,
    onSelectPoet: (Int?) -> Unit,
    onSelectCategory: (Int?) -> Unit,
) {
    if (showPoetPicker) {
        SearchOptionSheet(
            title = stringResource(Res.string.search_choose_poet),
            onDismiss = onDismissPoetPicker,
        ) {
            state.poetOptions.forEach { option ->
                SearchOptionRow(
                    title = if (option.id == null) allLabel else option.name,
                    depth = 0,
                    isSelected = option.id == state.selectedPoetId,
                    onClick = {
                        onSelectPoet(option.id)
                        onDismissPoetPicker()
                    },
                )
            }
        }
    }

    if (showCategoryPicker) {
        SearchOptionSheet(
            title = stringResource(Res.string.search_choose_category),
            onDismiss = onDismissCategoryPicker,
        ) {
            state.categoryOptions.forEach { option ->
                SearchOptionRow(
                    title = if (option.id == null) allLabel else option.title,
                    depth = option.depth,
                    isSelected = option.id == state.selectedCategoryId,
                    onClick = {
                        onSelectCategory(option.id)
                        onDismissCategoryPicker()
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchQueryField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
    ) {
        Icon(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            painter = painterResource(Res.drawable.search),
            contentDescription = stringResource(Res.string.search),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.search),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun SearchPickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = LocalSarvDimensions.current.dimen12, vertical = LocalSarvDimensions.current.dimen10),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResultUi,
    submittedQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                ).clickable(onClick = onClick)
                .padding(LocalSarvDimensions.current.dimen16),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
    ) {
        Text(
            text = "${result.poetName} · ${result.categoryName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = result.poemTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        HighlightedText(
            text = result.verseText,
            query = submittedQuery,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SearchOptionSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    SarvModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LocalSarvDimensions.current.dimen16)
                    .padding(bottom = LocalSarvDimensions.current.dimen24),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = LocalSarvDimensions.current.dimen8),
            )
            content()
        }
    }
}

@Composable
private fun SearchOptionRow(
    title: String,
    depth: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                .clickable(onClick = onClick)
                .padding(
                    start = (12 + depth * 16).dp,
                    top = LocalSarvDimensions.current.dimen10,
                    bottom = LocalSarvDimensions.current.dimen10,
                    end = LocalSarvDimensions.current.dimen12,
                ),
    )
}

@Preview
@Composable
private fun SearchScreenPreview() {
    SarvTheme {
        SearchScreen(
            state =
                SearchState(
                    query = "زلف",
                    submittedQuery = "زلف",
                    poetOptions =
                        listOf(
                            SearchPoetOptionUi(id = null, name = "همه"),
                            SearchPoetOptionUi(id = 2, name = "حافظ شیرازی"),
                        ),
                    categoryOptions =
                        listOf(
                            SearchCategoryOptionUi(id = null, title = "همه", depth = 0),
                            SearchCategoryOptionUi(id = 24, title = "غزلیات", depth = 0),
                        ),
                    selectedPoetId = 2,
                    selectedCategoryId = 24,
                    isCategoryPickerEnabled = true,
                ),
            searchResults =
                kotlinx.coroutines.flow
                    .flowOf(
                        androidx.paging.PagingData.from(
                            listOf(
                                SearchResultUi(
                                    poemId = 1,
                                    poemTitle = "غزل شماره ۱",
                                    poetName = "حافظ",
                                    categoryName = "غزلیات",
                                    verseText = "الا یا ایها الساقی",
                                    key = "1-1",
                                ),
                            ),
                        ),
                    ).collectAsLazyPagingItems(),
            onAction = {},
            onBackClick = {},
        )
    }
}
