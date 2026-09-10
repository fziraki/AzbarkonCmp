package abkabk.azbarkon.features.games.organizepoem

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val DRAG_SHADOW_ELEVATION = 8f

private val dragScaleSpring =
    spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

private val reorderSizeSpring =
    spring<IntSize>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

private val reorderPlacementSpring =
    spring<IntOffset>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy,
    )

@Composable
fun ReorderablePoemLines(
    items: List<String>,
    pinnedItemId: String?,
    enabled: Boolean,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        index: Int,
        itemId: String,
    ) -> Unit,
) {
    var draggingItemId by remember { mutableStateOf<String?>(null) }

    val currentItems by rememberUpdatedState(items)
    val currentPinnedItemId by rememberUpdatedState(pinnedItemId)
    val currentOnReorder by rememberUpdatedState(onReorder)
    val density = LocalDensity.current
    val thresholdPx = with(density) { LocalSarvDimensions.current.dimen48.toPx() }

    fun isLocked(index: Int): Boolean = currentItems.getOrNull(index) == currentPinnedItemId

    fun resolveToIndex(fromIndex: Int, direction: Int): Int {
        val adjacent = fromIndex + direction
        if (adjacent !in currentItems.indices) return adjacent
        return if (isLocked(adjacent)) fromIndex + direction * 2 else adjacent
    }

    LazyColumn(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = reorderSizeSpring),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
    ) {
        itemsIndexed(
            items = items,
            key = { _, itemId -> itemId },
        ) { index, itemId ->
            val isPinned = itemId == pinnedItemId
            val isDragging = draggingItemId == itemId
            val dragScale by animateFloatAsState(
                targetValue = if (isDragging) 1.02f else 1f,
                animationSpec = dragScaleSpring,
                label = "dragScale",
            )

            Box(
                modifier =
                    Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = reorderPlacementSpring,
                        )
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            scaleX = dragScale
                            scaleY = dragScale
                            if (isDragging) {
                                shadowElevation = DRAG_SHADOW_ELEVATION
                            }
                        }.then(
                            if (enabled && !isPinned) {
                                Modifier.pointerInput(itemId, enabled, isPinned) {
                                    var accumulatedDragY = 0f
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingItemId = itemId
                                            accumulatedDragY = 0f
                                        },
                                        onDragEnd = { draggingItemId = null },
                                        onDragCancel = { draggingItemId = null },
                                    ) { change, dragAmount ->
                                        change.consume()
                                        accumulatedDragY += dragAmount.y
                                        var fromIndex = currentItems.indexOf(itemId)
                                        while (fromIndex >= 0 && accumulatedDragY >= thresholdPx) {
                                            val toIndex = resolveToIndex(fromIndex, direction = 1)
                                            if (toIndex !in currentItems.indices || isLocked(toIndex)) break
                                            currentOnReorder(fromIndex, toIndex)
                                            accumulatedDragY -= thresholdPx
                                            fromIndex = currentItems.indexOf(itemId)
                                        }
                                        while (fromIndex >= 0 && accumulatedDragY <= -thresholdPx) {
                                            val toIndex = resolveToIndex(fromIndex, direction = -1)
                                            if (toIndex !in currentItems.indices || isLocked(toIndex)) break
                                            currentOnReorder(fromIndex, toIndex)
                                            accumulatedDragY += thresholdPx
                                            fromIndex = currentItems.indexOf(itemId)
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
            ) {
                content(index, itemId)
            }
        }
    }
}
