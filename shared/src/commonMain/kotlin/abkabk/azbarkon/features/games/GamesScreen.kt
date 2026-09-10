package abkabk.azbarkon.features.games

import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import abkabk.azbarkon.features.games.navigation.toRoute
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.complete_poem_desc
import sarv.shared.generated.resources.complete_poem_title
import sarv.shared.generated.resources.games
import sarv.shared.generated.resources.next_line_desc
import sarv.shared.generated.resources.next_line_title
import sarv.shared.generated.resources.poetry_arrangement_desc
import sarv.shared.generated.resources.poetry_arrangement_title
import sarv.shared.generated.resources.whois_poet_desc
import sarv.shared.generated.resources.whois_poet_title
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.ui.layout.ContentScale
import sarv.shared.generated.resources.guess_poet_icon
import sarv.shared.generated.resources.incomplete_icon
import sarv.shared.generated.resources.next_verse_icon
import sarv.shared.generated.resources.reorder_poem_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val GAME_CARD_IMAGE_WEIGHT = 0.3f
private const val GAME_CARD_CONTENT_WEIGHT = 0.7f

private class GameItemData(
    val title: StringResource,
    val desc: StringResource,
    val icon: DrawableResource,
)

@Composable
fun GamesRoot(
    onNavigateToGame: (GameType) -> Unit,
) {
    BaseScreen(screenState = UiScreenState.Success) {
        GamesScreen(onNavigateToGame = onNavigateToGame)
    }
}

@Composable
fun GamesScreen(
    onNavigateToGame: (GameType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns =
        when (LocalWindowSizeClass.current.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> GridCells.Fixed(2)
            WindowWidthSizeClass.Medium -> GridCells.Fixed(1)
            else -> GridCells.Fixed(1)
        }

    val gameItems =
        listOf(
            GameType.NEXT_VERSE to GameItemData(
                Res.string.next_line_title,
                Res.string.next_line_desc,
                Res.drawable.next_verse_icon,
            ),
            GameType.COMPLETE_POEM to GameItemData(
                Res.string.complete_poem_title,
                Res.string.complete_poem_desc,
                Res.drawable.incomplete_icon,
            ),
            GameType.FIND_POET to GameItemData(
                Res.string.whois_poet_title,
                Res.string.whois_poet_desc,
                Res.drawable.guess_poet_icon,
            ),
            GameType.ORGANIZE_POEM to GameItemData(
                Res.string.poetry_arrangement_title,
                Res.string.poetry_arrangement_desc,
                Res.drawable.reorder_poem_icon,
            ),
        )

    LazyVerticalGrid(
        columns = columns,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        contentPadding = PaddingValues(vertical = LocalSarvDimensions.current.dimen24, horizontal = LocalSarvDimensions.current.dimen16),
    ) {
        items(
            items = gameItems,
            key = { (gameType, _) -> gameType.name },
        ) { (gameType, data) ->
            GameItem(
                gameType = gameType,
                title = data.title,
                desc = data.desc,
                icon = data.icon,
                onClick = { onNavigateToGame(gameType) },
            )
        }
    }
}

@Composable
fun GameItem(
    gameType: GameType,
    title: StringResource,
    desc: StringResource,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min).clickable { onClick() },
        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12),
        tonalElevation = LocalSarvDimensions.current.dimen1,
        shadowElevation = LocalSarvDimensions.current.dimen1,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row {
            Image(
                modifier =
                    Modifier
                        .weight(GAME_CARD_IMAGE_WEIGHT)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        .clip(RoundedCornerShape(
                            topStart = LocalSarvDimensions.current.dimen12, bottomStart = LocalSarvDimensions.current.dimen12,
                            topEnd = 0.dp, bottomEnd = 0.dp
                        )),
                painter = painterResource(icon),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier =
                    Modifier
                        .weight(GAME_CARD_CONTENT_WEIGHT)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ).padding(LocalSarvDimensions.current.dimen16),
                verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(desc),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )

                Text(
                    modifier =
                        Modifier
                            .border(
                                width = LocalSarvDimensions.current.dimen1,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(LocalSarvDimensions.current.dimen8),
                            ).padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen4),
                    text = "${gameType.baseScore} امتیاز ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }

}

@Preview
@Composable
private fun GamesScreenPreview() {
    SarvTheme {
        GamesScreen(onNavigateToGame = {})
    }
}
