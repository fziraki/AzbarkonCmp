package abkabk.azbarkon.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun calculateWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return calculateWindowSizeClass(
        widthDp = configuration.screenWidthDp,
        heightDp = configuration.screenHeightDp
    )
}
