package abkabk.azbarkon.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class DeviceScaleInfo(
    val widthSizeClass: WindowWidthSizeClass,
    val widthDp: Int,
    val heightDp: Int
)

val LocalDeviceScaleInfo = staticCompositionLocalOf {
    DeviceScaleInfo(
        widthSizeClass = WindowWidthSizeClass.Compact,
        widthDp = 0,
        heightDp = 0
    )
}
