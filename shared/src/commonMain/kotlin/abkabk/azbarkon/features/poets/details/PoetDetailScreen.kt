package abkabk.azbarkon.features.poets.details

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.features.poets.PoetCategoryRowUi
import abkabk.azbarkon.features.poets.list.PoetAvatar
import abkabk.azbarkon.features.poets.list.PoetsSectionTitle
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.components.HeaderAction
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.cd_chat
import sarv.shared.generated.resources.chat_bubble
import sarv.shared.generated.resources.chat_with_poet
import sarv.shared.generated.resources.fal_button
import sarv.shared.generated.resources.poet_bio_read_less
import sarv.shared.generated.resources.poet_bio_read_more
import sarv.shared.generated.resources.poets_works_section
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass

@Composable
fun PoetDetailRoot(
    poetId: Int,
    onBackClick: () -> Unit,
    onNavigateToPoemList: (catId: Int, title: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToPoemDetail: (poemId: Int) -> Unit,
    viewModel: PoetDetailViewModel = koinViewModel { parametersOf(poetId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PoetDetailEvent.NavigateToPoemList -> {
                onNavigateToPoemList(event.catId, event.title)
            }

            is PoetDetailEvent.NavigateToChat -> onNavigateToChat()

            is PoetDetailEvent.NavigateToPoemDetail -> onNavigateToPoemDetail(event.poemId)
        }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        PoetDetailScreen(
            state = state,
            onAction = viewModel::onAction,
            onBackClick = onBackClick,
            onSearchClick = onNavigateToSearch
        )
    }
}

@Composable
fun PoetDetailScreen(
    state: PoetDetailState,
    onAction: (PoetDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Header(
            title = state.name,
            onBackClick = onBackClick,
            action = HeaderAction.Search(onSearchClick),
        )

        if (isExpanded) {
            Row(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = LocalSarvDimensions.current.dimen24),
                ) {
                    item {
                        PoetDetailHero(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier
                                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                                .padding(bottom = LocalSarvDimensions.current.dimen16),
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = LocalSarvDimensions.current.dimen24),
                ) {
                    poetCategoryItems(state.categories, onAction)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = LocalSarvDimensions.current.dimen24),
            ) {
                item {
                    PoetDetailHero(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier
                            .padding(horizontal = LocalSarvDimensions.current.dimen16)
                            .padding(bottom = LocalSarvDimensions.current.dimen16),
                    )
                }
                item {
                    PoetsSectionTitle(
                        title = stringResource(Res.string.poets_works_section),
                        modifier = Modifier
                            .padding(horizontal = LocalSarvDimensions.current.dimen16)
                            .padding(bottom = LocalSarvDimensions.current.dimen16),
                    )
                }
                poetCategoryItems(state.categories, onAction)
            }
        }
    }
}

private fun LazyListScope.poetCategoryItems(
    categories: List<PoetCategoryRowUi>,
    onAction: (PoetDetailAction) -> Unit,
) {
    itemsIndexed(
        items = categories,
        key = { _, category -> "${category.id}-${category.depth}" },
    ) { index, category ->
        PoetCategoryRow(
            category = category,
            onToggleClick = { onAction(PoetDetailAction.OnCategoryToggle(category.id)) },
            onLeafClick = {
                onAction(
                    PoetDetailAction.OnCategoryClick(
                        categoryId = category.id,
                        title = category.title,
                    ),
                )
            },
            modifier = Modifier
                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                .then(
                    if (index > 0) {
                        Modifier.padding(top = LocalSarvDimensions.current.dimen6)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@Composable
private fun PoetDetailHero(
    state: PoetDetailState,
    onAction: (PoetDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen20))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = LocalSarvDimensions.current.dimen1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(LocalSarvDimensions.current.dimen20),
                ).padding(LocalSarvDimensions.current.dimen20),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {
        PoetHeroInfo(
            state = state,
            onAction = onAction,
        )
        if (state.bio.isNotBlank()) {
            PoetBioText(
                bio = state.bio,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PoetHeroInfo(
    state: PoetDetailState,
    onAction: (PoetDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {
        PoetAvatar(
            imageUrl = state.imageUrl,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen96),
        )

        Text(
            text = state.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        if (state.canChat) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                        .clickable { onAction(PoetDetailAction.OnChatClick) }
                        .padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen4),
            ) {
                Text(
                    text = stringResource(Res.string.chat_with_poet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.chat_bubble),
                    contentDescription = stringResource(Res.string.cd_chat),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (state.canFal) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                        .clickable { onAction(PoetDetailAction.OnFalClick) }
                        .padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen4),
            ) {
                Text(
                    text = stringResource(Res.string.fal_button),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun PoetBioText(
    bio: String,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = bio,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layoutResult ->
                if (!isExpanded) {
                    isOverflowing = layoutResult.hasVisualOverflow
                }
            },
        )

        if (isOverflowing || isExpanded) {
            val interactionSource = remember { MutableInteractionSource() }
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource,
                        ) { isExpanded = !isExpanded },
                text = stringResource(
                    if (isExpanded) {
                        Res.string.poet_bio_read_less
                    } else {
                        Res.string.poet_bio_read_more
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun PoetDetailScreenPreview() {
    SarvTheme {
        PoetDetailScreen(
            state =
                PoetDetailState(
                    name = "حافظ شیرازی",
                    bio =
                        "خواجه شمس‌الدین محمد بن بهاءالدین حافظ شیرازی، " +
                            "شاعر بزرگ ایرانی و غزل‌سرای نامدار سدهٔ هفتم هجری است. " +
                            "دیوان اشعار او از مهم‌ترین آثار ادبی فارسی به‌شمار می‌رود " +
                            "و بسیاری از اشعارش در میان مردم ایران و جهان فارسی‌زبان " +
                            "شناخته‌شده و مورد استفاده قرار می‌گیرد.",
                    categories =
                        listOf(
                            PoetCategoryRowUi(
                                id = 24,
                                title = "غزلیات",
                                depth = 0,
                                isParent = true,
                                isExpanded = false,
                            ),
                            PoetCategoryRowUi(
                                id = 25,
                                title = "قطعات",
                                depth = 0,
                                isParent = false,
                                isExpanded = false,
                            ),
                        ),
                ),
            onAction = {},
            onBackClick = {},
            onSearchClick = {}
        )
    }
}
