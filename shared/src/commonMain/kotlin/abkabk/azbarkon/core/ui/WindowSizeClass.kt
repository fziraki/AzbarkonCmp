package abkabk.azbarkon.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
enum class WindowWidthSizeClass { Compact, Medium, Expanded }

@Immutable
class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val widthDp: Int,
    val heightDp: Int,
)

val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass(
        widthSizeClass = WindowWidthSizeClass.Compact,
        widthDp = 0,
        heightDp = 0
    )
}

private const val MEDIUM_MIN_DP = 600
private const val EXPANDED_MIN_DP = 840

fun calculateWindowSizeClass(widthDp: Int, heightDp: Int): WindowSizeClass {
    val widthClass = when {
        widthDp >= EXPANDED_MIN_DP -> WindowWidthSizeClass.Expanded
        widthDp >= MEDIUM_MIN_DP -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Compact
    }
    return WindowSizeClass(
        widthSizeClass = widthClass,
        widthDp = widthDp,
        heightDp = heightDp
    )
}

@Composable
expect fun calculateWindowSizeClass(): WindowSizeClass
