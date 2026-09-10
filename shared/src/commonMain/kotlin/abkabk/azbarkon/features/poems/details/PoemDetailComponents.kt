package abkabk.azbarkon.features.poems.details

import abkabk.azbarkon.core.ui.HighlightedText
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.add_box_24px
import sarv.shared.generated.resources.cd_add_poem
import sarv.shared.generated.resources.cd_context_search
import sarv.shared.generated.resources.cd_image_creator
import sarv.shared.generated.resources.cd_like
import sarv.shared.generated.resources.cd_share
import sarv.shared.generated.resources.heart
import sarv.shared.generated.resources.heart_filled
import sarv.shared.generated.resources.ornoment230l
import sarv.shared.generated.resources.ornoment230r
import sarv.shared.generated.resources.ornoment30
import sarv.shared.generated.resources.palette
import sarv.shared.generated.resources.poem_image_creator
import sarv.shared.generated.resources.poem_liked
import sarv.shared.generated.resources.poem_memorize
import sarv.shared.generated.resources.poem_share
import sarv.shared.generated.resources.search
import sarv.shared.generated.resources.share
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn

@Composable
fun PoemVerseItem(
    verse: PoemVerseUi,
    modifier: Modifier = Modifier,
    highlightQuery: String = "",
) {
    when (verse.positionType) {
        PoemVersePositionType.Comment -> {
            CommentVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }

        PoemVersePositionType.Right -> {
            RightVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }

        PoemVersePositionType.Left -> {
            LeftVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }

        PoemVersePositionType.Center -> {
            CenterVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }

        PoemVersePositionType.Paragraph -> {
            ParagraphVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }

        PoemVersePositionType.Single -> {
            SingleVerseItem(
                text = verse.text,
                highlightQuery = highlightQuery,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CommentVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = LocalSarvDimensions.current.dimen8)
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = LocalSarvDimensions.current.dimen12, vertical = LocalSarvDimensions.current.dimen10),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun RightVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = LocalSarvDimensions.current.dimen16),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Start,
    )
}

@Composable
private fun LeftVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = LocalSarvDimensions.current.dimen12),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun CenterVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = LocalSarvDimensions.current.dimen12),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ParagraphVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = LocalSarvDimensions.current.dimen8),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SingleVerseItem(
    text: String,
    highlightQuery: String,
    modifier: Modifier = Modifier,
) {
    HighlightedText(
        text = text,
        query = highlightQuery,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = LocalSarvDimensions.current.dimen16),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun PoemOrnamentalDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            LocalSarvDimensions.current.dimen4, Alignment.CenterHorizontally),
    ) {
        Image(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen32),
            painter = painterResource(Res.drawable.ornoment230r),
            contentDescription = null,
        )
        Image(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen48),
            painter = painterResource(Res.drawable.ornoment30),
            contentDescription = null,
        )
        Image(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen32),
            painter = painterResource(Res.drawable.ornoment230l),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun PoemOrnamentalDividerPreview() {
    SarvTheme {
        PoemOrnamentalDivider()
    }
}

@Composable
fun PoemActionBar(
    isLiked: Boolean,
    isProse: Boolean,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    onLikeClick: () -> Unit,
    onImageCreatorClick: () -> Unit,
    onMemorizeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    if (isExpanded) {
        LazyColumn(
            modifier =
                modifier
                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = LocalSarvDimensions.current.dimen1,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
                    ).padding(vertical = LocalSarvDimensions.current.dimen8, horizontal = LocalSarvDimensions.current.dimen6),
            verticalArrangement = Arrangement.spacedBy(
                space = LocalSarvDimensions.current.dimen16,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(LocalSarvDimensions.current.dimen4)
        ) {
            item {
            PoemActionItem(
                icon = if (isLiked) Res.drawable.heart_filled else Res.drawable.heart,
                label = Res.string.poem_liked,
                contentDescription = Res.string.cd_like,
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                labelColor = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onLikeClick,
            )}

            item {
            PoemActionItem(
                icon = Res.drawable.palette,
                label = Res.string.poem_image_creator,
                contentDescription = Res.string.cd_image_creator,
                onClick = onImageCreatorClick,
            )}

            if (!isProse) {
                item {
                PoemActionItem(
                    icon = Res.drawable.add_box_24px,
                    label = Res.string.poem_memorize,
                    contentDescription = Res.string.cd_add_poem,
                    onClick = onMemorizeClick,
                    tint = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.primary,
                )}
            }

            item {
            PoemActionItem(
                icon = Res.drawable.share,
                label = Res.string.poem_share,
                contentDescription = Res.string.cd_share,
                onClick = onShareClick,
            )}

            item {
            PoemActionItem(
                icon = Res.drawable.search,
                label = Res.string.search,
                contentDescription = Res.string.cd_context_search,
                onClick = onSearchClick,
            )}
        }
    } else {
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
                    ).padding(vertical = LocalSarvDimensions.current.dimen8, horizontal = LocalSarvDimensions.current.dimen6),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            PoemActionItem(
                icon = if (isLiked) Res.drawable.heart_filled else Res.drawable.heart,
                label = Res.string.poem_liked,
                contentDescription = Res.string.cd_like,
                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                labelColor = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onLikeClick,
            )


            PoemActionItem(
                icon = Res.drawable.palette,
                label = Res.string.poem_image_creator,
                contentDescription = Res.string.cd_image_creator,
                onClick = onImageCreatorClick,
            )


            if (!isProse) {
                PoemActionItem(
                    icon = Res.drawable.add_box_24px,
                    label = Res.string.poem_memorize,
                    contentDescription = Res.string.cd_add_poem,
                    onClick = onMemorizeClick,
                    tint = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.primary,
                )
            }

            PoemActionItem(
                icon = Res.drawable.share,
                label = Res.string.poem_share,
                contentDescription = Res.string.cd_share,
                onClick = onShareClick,
            )

            PoemActionItem(
                icon = Res.drawable.search,
                label = Res.string.search,
                contentDescription = Res.string.cd_context_search,
                onClick = onSearchClick,
            )
        }
    }
}

@Composable
private fun PoemActionItem(
    icon: DrawableResource,
    label: StringResource,
    contentDescription: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .clickable(onClick = onClick)
                .padding(vertical = LocalSarvDimensions.current.dimen4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(contentDescription),
            tint = tint,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen22),
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}
