package abkabk.azbarkon.ui.components

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <T> AnimatedTabRow(
    selectedTab: T,
    onSelectTab: (T) -> Unit,
    tabTitles: List<String>,
    tabs: List<T>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LocalSarvDimensions.current.dimen48),
        ) {
            tabs.forEachIndexed { index, tab ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectTab(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tabTitles[index],
                        style = MaterialTheme.typography.labelLarge,
                        color =
                            if (selectedTab == tab) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LocalSarvDimensions.current.dimen4)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabWidth = maxWidth / tabs.size
                val selectedIndex = tabs.indexOf(selectedTab)
                val indicatorOffset by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = tween(durationMillis = 200),
                )

                Box(
                    modifier =
                        Modifier
                            .width(tabWidth)
                            .fillMaxHeight()
                            .offset(x = indicatorOffset)
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
