package abkabk.azbarkon.features.profile

import abkabk.azbarkon.domain.model.profile.BadgeUi
import abkabk.azbarkon.domain.model.profile.GameProfileStats
import abkabk.azbarkon.domain.model.profile.MemorizationProfileStats
import abkabk.azbarkon.domain.model.profile.ProfileLevelProgress
import abkabk.azbarkon.ui.theme.LightColorScheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.cd_levels
import sarv.shared.generated.resources.ic_help
import sarv.shared.generated.resources.distich100_badge
import sarv.shared.generated.resources.first_badge
import sarv.shared.generated.resources.hafez_star_badge
import sarv.shared.generated.resources.level1
import sarv.shared.generated.resources.level2
import sarv.shared.generated.resources.level3
import sarv.shared.generated.resources.level4
import sarv.shared.generated.resources.level5
import sarv.shared.generated.resources.level6
import sarv.shared.generated.resources.level7
import sarv.shared.generated.resources.level8
import sarv.shared.generated.resources.night_badge
import sarv.shared.generated.resources.poetry_lover_badge
import sarv.shared.generated.resources.profile_game_status_title
import sarv.shared.generated.resources.profile_game_streak
import sarv.shared.generated.resources.profile_game_total_points
import sarv.shared.generated.resources.profile_level_format
import sarv.shared.generated.resources.memorization_active_poems
import sarv.shared.generated.resources.memorization_completed_poems
import sarv.shared.generated.resources.profile_memorization_status_title
import sarv.shared.generated.resources.profile_view_all_badges
import sarv.shared.generated.resources.profile_xp_format
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val WEEKLY_STREAK_BADGE_ID = 3
private const val POETRY_LOVER_BADGE_ID = 4
private const val PERFECT_GAMES_BADGE_ID = 5

internal val levelImages: List<DrawableResource> =
    listOf(
        Res.drawable.level1,
        Res.drawable.level2,
        Res.drawable.level3,
        Res.drawable.level4,
        Res.drawable.level5,
        Res.drawable.level6,
        Res.drawable.level7,
        Res.drawable.level8,
    )

internal fun levelImageResource(levelId: Int): DrawableResource? =
    levelImages.getOrNull(levelId - 1)

@Composable
fun ProfileHeader(
    levelProgress: ProfileLevelProgress,
    onLevelsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        levelImageResource(levelProgress.levelId)?.let { drawable ->
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen96).clickable(onClick = onLevelsClick),
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLevelsClick),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4, Alignment.CenterHorizontally ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(Res.string.profile_level_format, levelProgress.levelId),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.size(LocalSarvDimensions.current.dimen2)
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ))
            Text(
                text = levelProgress.levelName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = onLevelsClick,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen32),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_help),
                    contentDescription = stringResource(Res.string.cd_levels),
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
                )
            }
        }


        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            LinearProgressIndicator(
                progress = {
                    if (levelProgress.targetXp == 0) {
                        0f
                    } else {
                        levelProgress.currentXp.toFloat() / levelProgress.targetXp.toFloat()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalSarvDimensions.current.dimen32)
                        .height(LocalSarvDimensions.current.dimen8),
                trackColor = LightColorScheme.outlineVariant,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
                gapSize = (-4).dp,
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text =
                stringResource(
                    Res.string.profile_xp_format,
                    levelProgress.currentXp,
                    levelProgress.targetXp,
                ),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun MemorizationStatusCard(
    stats: MemorizationProfileStats,
    modifier: Modifier = Modifier,
) {
    ProfileStatusCard(
        modifier = modifier,
        title = stringResource(Res.string.profile_memorization_status_title),
        items =
            listOf(
                stats.activePoemCount to stringResource(Res.string.memorization_active_poems),
                stats.completedPoemCount to stringResource(Res.string.memorization_completed_poems),
            ),
    )
}

@Composable
fun GameStatusCard(
    stats: GameProfileStats,
    modifier: Modifier = Modifier,
) {
    ProfileStatusCard(
        modifier = modifier,
        title = stringResource(Res.string.profile_game_status_title),
        items =
            listOf(
                stats.visitStreak to stringResource(Res.string.profile_game_streak),
                stats.coinBalance to stringResource(Res.string.profile_game_total_points),
            ),
    )
}

@Composable
private fun ProfileStatusCard(
    title: String,
    items: List<Pair<Int, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12),
        tonalElevation = LocalSarvDimensions.current.dimen1,
        shadowElevation = LocalSarvDimensions.current.dimen1,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(LocalSarvDimensions.current.dimen16),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Start,
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
            ) {
                items.forEachIndexed { index, (value, label) ->
                    ProfileStatItem(
                        modifier = Modifier.weight(1f),
                        value = value,
                        label = label,
                    )
                    if (index < items.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier.fillMaxHeight(),
                            thickness = LocalSarvDimensions.current.dimen1,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun ProfileStatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = LocalSarvDimensions.current.dimen4),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ProfileBadges(
    badges: List<BadgeUi>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalSarvDimensions.current.dimen12),
        tonalElevation = LocalSarvDimensions.current.dimen1,
        shadowElevation = LocalSarvDimensions.current.dimen1,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = LocalSarvDimensions.current.dimen16),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                modifier = Modifier.clickable(onClick = onViewAllClick).padding(end = LocalSarvDimensions.current.dimen16),
                text = stringResource(Res.string.profile_view_all_badges),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.End,
            )

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(
                    items = badges,
                    key = { badge -> badge.id },
                ) { badge ->
                    BadgeItem(item = badge)
                }
            }
        }
    }


}

@Composable
fun BadgeItem(
    item: BadgeUi,
    modifier: Modifier = Modifier,
) {
    BadgeIcon(
        badgeId = item.id,
        isEarned = item.isEarned,
        modifier = modifier.width(LocalSarvDimensions.current.dimen80),
        showName = true,
        name = item.name,
    )
}

@Composable
fun BadgeListRow(
    item: BadgeUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
    ) {
        BadgeIcon(
            badgeId = item.id,
            isEarned = item.isEarned,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen56),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (item.isEarned) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    },
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (item.isEarned) 1f else 0.45f),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BadgeIcon(
    badgeId: Int,
    isEarned: Boolean,
    modifier: Modifier = Modifier,
    showName: Boolean = false,
    name: String = "",
) {
    val drawable =
        when (badgeId) {
            1 -> Res.drawable.first_badge
            2 -> Res.drawable.distich100_badge
            WEEKLY_STREAK_BADGE_ID -> Res.drawable.night_badge
            POETRY_LOVER_BADGE_ID -> Res.drawable.poetry_lover_badge
            PERFECT_GAMES_BADGE_ID -> Res.drawable.hafez_star_badge
            else -> Res.drawable.first_badge
        }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = if (showName) Modifier else Modifier.fillMaxSize(),
            colorFilter =
                if (isEarned) {
                    null
                } else {
                    ColorFilter.colorMatrix(
                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) },
                    )
                },
            alpha = if (isEarned) 1f else 0.45f,
        )
        if (showName) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color =
                    if (isEarned) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    },
                textAlign = TextAlign.Center,
            )
        }
    }
}
