package abkabk.azbarkon.features.games.components

import abkabk.azbarkon.domain.model.games.GameType
import abkabk.azbarkon.ui.components.ShimmerPlaceholder
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val SHIMMER_ROW_COUNT = 4

@Composable
fun GameContentShimmer(
    gameType: GameType,
    modifier: Modifier = Modifier,
) {
    when (gameType) {
        GameType.NEXT_VERSE -> NextVerseContentShimmer(modifier)
        GameType.FIND_POET -> FindPoetContentShimmer(modifier)
        GameType.COMPLETE_POEM -> CompletePoemContentShimmer(modifier)
        GameType.ORGANIZE_POEM -> OrganizePoemContentShimmer(modifier)
    }
}

@Composable
private fun NextVerseContentShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        GamePoemCard {
            ShimmerBar(height = LocalSarvDimensions.current.dimen16, widthFraction = 0.35f)
            ShimmerBar(height = LocalSarvDimensions.current.dimen20)
            ShimmerBar(height = LocalSarvDimensions.current.dimen20)
        }

        ShimmerInstructionBar()

        repeat(SHIMMER_ROW_COUNT) {
            ShimmerBar(
                height = LocalSarvDimensions.current.dimen48,
                shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12),
            )
        }
    }
}

@Composable
private fun FindPoetContentShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        GamePoemCard {
            ShimmerBar(height = LocalSarvDimensions.current.dimen16, widthFraction = 0.35f)
            Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
                ShimmerBar(height = LocalSarvDimensions.current.dimen20)
                ShimmerBar(height = LocalSarvDimensions.current.dimen20)
            }
        }

        ShimmerInstructionBar()

        Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                ) {
                    repeat(2) {
                        Row(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                                    .padding(LocalSarvDimensions.current.dimen12),
                            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ShimmerCircle(size = LocalSarvDimensions.current.dimen36)
                            ShimmerBar(
                                modifier = Modifier.weight(1f),
                                height = LocalSarvDimensions.current.dimen16,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletePoemContentShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        GamePoemCard {
            ShimmerBar(height = LocalSarvDimensions.current.dimen16, widthFraction = 0.35f)
            Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
                ShimmerBar(height = LocalSarvDimensions.current.dimen20)
                ShimmerBar(height = LocalSarvDimensions.current.dimen20)
            }
        }

        ShimmerInstructionBar()

        Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                ) {
                    repeat(2) {
                        ShimmerBar(
                            modifier = Modifier.weight(1f),
                            height = LocalSarvDimensions.current.dimen48,
                            shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizePoemContentShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {
        GamePoemCard {
            ShimmerBar(height = LocalSarvDimensions.current.dimen16, widthFraction = 0.35f)
            Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
                repeat(SHIMMER_ROW_COUNT) {
                    ShimmerBar(height = LocalSarvDimensions.current.dimen20)
                }
            }
        }

        ShimmerInstructionBar()

        Column(verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)) {
            repeat(SHIMMER_ROW_COUNT) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                            .padding(LocalSarvDimensions.current.dimen12),
                    horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShimmerBar(
                        height = LocalSarvDimensions.current.dimen24,
                        widthFraction = 0.08f,
                        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen4),
                    )
                    ShimmerBar(
                        modifier = Modifier.weight(1f),
                        height = LocalSarvDimensions.current.dimen16,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShimmerInstructionBar(
    height: Dp = LocalSarvDimensions.current.dimen16,
    widthFraction: Float = 0.6f,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        ShimmerBar(height = height, widthFraction = widthFraction)
    }
}

@Composable
private fun ShimmerBar(
    height: Dp,
    modifier: Modifier = Modifier,
    widthFraction: Float? = null,
    shape: Shape = RoundedCornerShape(LocalSarvDimensions.current.dimen8),
) {
    Box(
        modifier =
            modifier
                .then(
                    if (widthFraction != null) {
                        Modifier.fillMaxWidth(widthFraction)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ).height(height)
                .clip(shape),
    ) {
        ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun ShimmerCircle(size: Dp) {
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape),
    ) {
        ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun NextVerseContentShimmerPreview() {
    SarvTheme {
        GameContentShimmer(
            gameType = GameType.NEXT_VERSE,
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}

@Preview
@Composable
private fun FindPoetContentShimmerPreview() {
    SarvTheme {
        GameContentShimmer(
            gameType = GameType.FIND_POET,
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}

@Preview
@Composable
private fun CompletePoemContentShimmerPreview() {
    SarvTheme {
        GameContentShimmer(
            gameType = GameType.COMPLETE_POEM,
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}

@Preview
@Composable
private fun OrganizePoemContentShimmerPreview() {
    SarvTheme {
        GameContentShimmer(
            gameType = GameType.ORGANIZE_POEM,
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}
