package abkabk.azbarkon.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.arrow_back_right
import sarv.shared.generated.resources.bookmark
import sarv.shared.generated.resources.bookmark_filled
import sarv.shared.generated.resources.cd_back
import sarv.shared.generated.resources.cd_bookmark
import sarv.shared.generated.resources.cd_memorization_review_alarm
import sarv.shared.generated.resources.cd_search
import sarv.shared.generated.resources.clear_all_title
import sarv.shared.generated.resources.notifications
import sarv.shared.generated.resources.search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    action: HeaderAction? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            Box(
                modifier =
                    Modifier
                        .size(LocalSarvDimensions.current.dimen40)
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.arrow_back_right),
                    contentDescription = stringResource(Res.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Box(modifier = Modifier.size(LocalSarvDimensions.current.dimen40))
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen2),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (action != null) {
            HeaderActionButton(action)
        } else {
            Box(modifier = Modifier.size(LocalSarvDimensions.current.dimen40))
        }
    }
}

@Composable
private fun HeaderActionButton(action: HeaderAction) {
    if (action is HeaderAction.ClearAll) {
        Text(
            text = stringResource(Res.string.clear_all_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                    .clickable(onClick = action.onClick)
                    .padding(horizontal = LocalSarvDimensions.current.dimen8, vertical = LocalSarvDimensions.current.dimen8),
        )
        return
    }

    Box(
        modifier =
            Modifier
                .size(LocalSarvDimensions.current.dimen40)
                .clip(CircleShape)
                .clickable(onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (action) {
            is HeaderAction.Search -> {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.search),
                    contentDescription = stringResource(Res.string.cd_search),
                )
            }

            is HeaderAction.Bookmark -> {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter =
                        painterResource(
                            if (action.isBookmarked) {
                                Res.drawable.bookmark_filled
                            } else {
                                Res.drawable.bookmark
                            },
                        ),
                    contentDescription = stringResource(Res.string.cd_bookmark),
                    tint =
                        if (action.isBookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            is HeaderAction.Alarm -> {
                Icon(
                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                    painter = painterResource(Res.drawable.notifications),
                    contentDescription = stringResource(Res.string.cd_memorization_review_alarm),
                    tint =
                        if (action.isEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            is HeaderAction.ClearAll -> Unit
        }
    }
}
