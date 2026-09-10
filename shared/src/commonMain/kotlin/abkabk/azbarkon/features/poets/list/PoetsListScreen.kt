package abkabk.azbarkon.features.poets.list

import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.features.poets.FeaturedPoetUi
import abkabk.azbarkon.features.poets.PoetListItemUi
import abkabk.azbarkon.ui.components.SarvButton
import abkabk.azbarkon.ui.theme.SarvTheme
import abkabk.azbarkon.ui.theme.LightColorScheme
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.cd_chat
import sarv.shared.generated.resources.chat_bubble
import sarv.shared.generated.resources.download
import sarv.shared.generated.resources.poets_download
import sarv.shared.generated.resources.poets_filter_placeholder
import sarv.shared.generated.resources.poets_view_works
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun PoetsListRoot(
    onNavigateToPoetDetail: (Int) -> Unit,
    onNavigateToChat: (Int) -> Unit,
    viewModel: PoetsListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PoetsListEvent.NavigateToPoetDetail -> onNavigateToPoetDetail(event.poetId)
            is PoetsListEvent.NavigateToChat -> onNavigateToChat(event.poetId)
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onAction(PoetsListAction.OnScreenEnter)
        onPauseOrDispose { }
    }

    BaseScreen(
        screenState = state.screenState,
    ) {
        PoetsListScreen(
            state = state,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
fun PoetsListScreen(
    state: PoetsListState,
    onAction: (PoetsListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns =
        when (LocalWindowSizeClass.current.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> GridCells.Fixed(2)
            WindowWidthSizeClass.Medium -> GridCells.Fixed(1)
            else -> GridCells.Fixed(1)
        }

    Column(
        modifier =
            modifier
                .fillMaxSize().padding(top = LocalSarvDimensions.current.dimen16)
                .background(MaterialTheme.colorScheme.background),
    ) {

        FilterField(
            value = state.searchQuery,
            placeholder = stringResource(Res.string.poets_filter_placeholder),
            onValueChange = { onAction(PoetsListAction.OnSearchQueryChange(it)) },
            modifier = Modifier.padding(horizontal = LocalSarvDimensions.current.dimen16),
        )

        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                vertical = LocalSarvDimensions.current.dimen24,
                horizontal = LocalSarvDimensions.current.dimen16,
            ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        ) {
            state.featuredPoet?.let { featured ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedPoetCard(
                        poet = featured,
                        onClick = { onAction(PoetsListAction.OnFeaturedPoetClick) },
                        onChatClick = { onAction(PoetsListAction.OnChatClick(featured.id)) },
                    )
                }
            }

            items(
                items = state.poets,
                key = { poet -> poet.id },
            ) { poet ->
                PoetListRow(
                    poet = poet,
                    isDownloading = poet.id in state.downloadingPoetIds,
                    onClick = {
                        if (poet.isDownloaded) {
                            onAction(PoetsListAction.OnPoetClick(poet.id))
                        } else {
                            onAction(PoetsListAction.OnDownloadPoet(poet.id))
                        }
                    },
                    onDownloadClick = { onAction(PoetsListAction.OnDownloadPoet(poet.id)) },
                    onChatClick = { onAction(PoetsListAction.OnChatClick(poet.id)) },
                )
            }
        }
    }
}

@Composable
private fun FeaturedPoetCard(
    poet: FeaturedPoetUi,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen20))
                .background(LightColorScheme.primary)
                .padding(LocalSarvDimensions.current.dimen20),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                    .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PoetAvatar(
                imageUrl = poet.imageUrl,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen72),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
            ) {
                Text(
                    text = poet.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = LightColorScheme.onPrimary,
                )
                Text(
                    text = poet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightColorScheme.onPrimary.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (poet.stats.isNotBlank()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = poet.stats,
                        style = MaterialTheme.typography.labelMedium,
                        color = LightColorScheme.surfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (poet.canChat) {
                Icon(
                    painter = painterResource(Res.drawable.chat_bubble),
                    contentDescription = stringResource(Res.string.cd_chat),
                    tint = LightColorScheme.surfaceVariant,
                    modifier =
                        Modifier
                            .size(LocalSarvDimensions.current.dimen24)
                            .clip(CircleShape)
                            .clickable(onClick = onChatClick)
                            .padding(LocalSarvDimensions.current.dimen4),
                )
            }

            SarvButton(
                text = stringResource(Res.string.poets_view_works),
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightColorScheme.secondary,
                    contentColor = LightColorScheme.onSecondary,
                    disabledContainerColor = LightColorScheme.secondary,
                    disabledContentColor = LightColorScheme.onSecondary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

    }
}

@Composable
private fun PoetListRow(
    poet: PoetListItemUi,
    isDownloading: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation =  CardDefaults.cardElevation(defaultElevation = LocalSarvDimensions.current.dimen1)
    ){
        Row(
            modifier = Modifier.padding(horizontal = LocalSarvDimensions.current.dimen16, vertical = LocalSarvDimensions.current.dimen12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen20),
        ) {

            PoetAvatar(
                imageUrl = poet.imageUrl,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen56),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = poet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                )
                if (poet.worksSummary.isNotBlank()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = poet.worksSummary,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!poet.isDownloaded) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                        strokeWidth = LocalSarvDimensions.current.dimen2,
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.download),
                        contentDescription = stringResource(Res.string.poets_download),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(LocalSarvDimensions.current.dimen24)
                                .clip(CircleShape)
                                .clickable(onClick = onDownloadClick)
                                .padding(LocalSarvDimensions.current.dimen4),
                    )
                }
            } else if (poet.canChat) {
                Icon(
                    painter = painterResource(Res.drawable.chat_bubble),
                    contentDescription = stringResource(Res.string.cd_chat),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .size(LocalSarvDimensions.current.dimen24)
                            .clip(CircleShape)
                            .clickable(onClick = onChatClick)
                            .padding(LocalSarvDimensions.current.dimen4),
                )
            }
        }
    }

}

@Preview
@Composable
private fun PoetsListScreenPreview() {
    SarvTheme {
        PoetsListScreen(
            state =
                PoetsListState(
                    poets =
                        listOf(
                            PoetListItemUi(
                                id = 7,
                                name = "سعدی شیرازی",
                                worksSummary = "گلستان و 1 اثر دیگر",
                                imageUrl = null,
                                canChat = false,
                                isDownloaded = false,
                            ),
                        ),
                    featuredPoet =
                        FeaturedPoetUi(
                            id = 2,
                            name = "حافظ شیرازی",
                            description = "غزل‌سرای بزرگ ایران",
                            stats = "قطعات و 4 اثر دیگر",
                            imageUrl = null,
                            canChat = true,
                        ),
                ),
            onAction = {},
        )
    }
}
