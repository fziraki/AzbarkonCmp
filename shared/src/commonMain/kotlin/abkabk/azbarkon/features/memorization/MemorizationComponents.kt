package abkabk.azbarkon.features.memorization

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.ui.theme.LightColorScheme
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.feather
import sarv.shared.generated.resources.forward
import sarv.shared.generated.resources.ic_delete
import sarv.shared.generated.resources.memorization_cards_progress_format
import sarv.shared.generated.resources.memorization_quick_start
import sarv.shared.generated.resources.memorization_quick_start_couplet
import sarv.shared.generated.resources.memorization_quick_start_desc
import sarv.shared.generated.resources.memorization_quick_start_ghazal
import sarv.shared.generated.resources.memorization_quick_start_rubaiyat
import sarv.shared.generated.resources.memorization_re_review
import sarv.shared.generated.resources.memorization_review_info_format
import sarv.shared.generated.resources.memorization_select_hero_subtitle
import sarv.shared.generated.resources.memorization_select_hero_title
import sarv.shared.generated.resources.ornoment30
import sarv.shared.generated.resources.search

private const val LABEL_WIDTH_FRACTION = 0.9f

@Composable
fun MemorizationHeroSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
    ) {

        Box(contentAlignment = Alignment.Center){

            Box(
                modifier = Modifier.size(LocalSarvDimensions.current.dimen92)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
            )

        Image(
            painter = painterResource(Res.drawable.feather),
            contentDescription = null,
        )
        }

        Text(
            text = stringResource(Res.string.memorization_select_hero_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(Res.string.memorization_select_hero_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun QuickStartCard(
    onCoupletClick: () -> Unit,
    onGhazalClick: () -> Unit,
    onRubaiyatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen20))
                .background(LightColorScheme.primary)
                .padding(LocalSarvDimensions.current.dimen20),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.memorization_quick_start),
            style = MaterialTheme.typography.titleLarge,
            color = LightColorScheme.onPrimary,
        )

        Text(
            text = stringResource(Res.string.memorization_quick_start_desc),
            style = MaterialTheme.typography.bodySmall,
            color = LightColorScheme.onPrimary.copy(alpha = 0.85f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            QuickStartCategoryTile(
                label = stringResource(Res.string.memorization_quick_start_couplet),
                onClick = onCoupletClick,
                modifier = Modifier.weight(1f)
            )
            QuickStartCategoryTile(
                label = stringResource(Res.string.memorization_quick_start_ghazal),
                onClick = onGhazalClick,
                modifier = Modifier.weight(1f)
            )
            QuickStartCategoryTile(
                label = stringResource(Res.string.memorization_quick_start_rubaiyat),
                onClick = onRubaiyatClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickStartCategoryTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen12))
                .clickable(onClick = onClick)
                .padding(LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        Box(
            modifier =
                Modifier
                    .size(LocalSarvDimensions.current.dimen56)
                    .clip(CircleShape)
                    .background(LightColorScheme.surface.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ornoment30),
                contentDescription = null,
                tint = LightColorScheme.onPrimary,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen40),
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LightColorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(LABEL_WIDTH_FRACTION),
        )
    }
}

@Composable
fun MemorizationOptionRow(
    title: String,
    description: String,
    icon: DrawableResource,
    onClick: () -> Unit,
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
                ).clickable(onClick = onClick)
                .padding(LocalSarvDimensions.current.dimen16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
    ) {


        Box(
            modifier =
                Modifier
                    .size(LocalSarvDimensions.current.dimen48)
                    .clip(CircleShape)
                    .background(LightColorScheme.secondary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = LightColorScheme.onSecondary,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            painter = painterResource(Res.drawable.forward),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.scale(scaleX = -1f, scaleY = 1f).size(LocalSarvDimensions.current.dimen20),
        )

    }
}

@Suppress("LongParameterList")
@Composable
fun PoemCard(
    title: String,
    poetName: String,
    reviewCount: Int,
    nextReviewDays: Int,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalCards: Int = 0,
    reviewedCards: Int = 0,
) {
    val progress =
        if (totalCards == 0) 0f
        else (reviewedCards.toFloat() / totalCards.toFloat()).coerceIn(0f, 1f)
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
                ).clickable(onClick = onClick)
                .padding(LocalSarvDimensions.current.dimen16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen6),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Icon(
                    modifier = Modifier.clickable{ onDeleteClick() }
                        .size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = poetName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                        stringResource(
                            Res.string.memorization_review_info_format,
                            reviewCount,
                            nextReviewDays,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = stringResource(Res.string.memorization_cards_progress_format, reviewedCards, totalCards),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (!isCompleted){
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = LightColorScheme.outlineVariant,
                    gapSize = (-4).dp
                )
            }else{
                Text(
                    modifier = Modifier.clickable{ onReReviewClick()},
                    text =
                        stringResource(
                            Res.string.memorization_re_review,
                            reviewCount,
                            nextReviewDays,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

        }
    }
}

@Preview
@Composable
private fun MemorizationHeroSectionPreview() {
    SarvTheme {
        MemorizationHeroSection(modifier = Modifier.padding(LocalSarvDimensions.current.dimen16))
    }
}

@Preview
@Composable
private fun QuickStartCardPreview() {
    SarvTheme {
        QuickStartCard(
            onCoupletClick = {},
            onGhazalClick = {},
            onRubaiyatClick = {},
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}

@Preview
@Composable
private fun MemorizationOptionRowPreview() {
    SarvTheme {
        MemorizationOptionRow(
            title = "جستجوی شعر",
            description = "شاعر، بیت یا نام شعر را جستجو کنید",
            icon = Res.drawable.search,
            onClick = {},
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
        )
    }
}

@Preview
@Composable
private fun PoemCardPreview() {
    SarvTheme {
        PoemCard(
            title = "غزل ۱",
            poetName = "حافظ",
            reviewCount = 5,
            nextReviewDays = 3,
            isCompleted = false,
            onClick = {},
            onDeleteClick = {},
            onReReviewClick = {},
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
            totalCards = 10,
            reviewedCards = 4,
        )
    }
}
