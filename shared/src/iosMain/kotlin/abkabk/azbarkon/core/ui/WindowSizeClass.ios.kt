package abkabk.azbarkon.core.ui

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun calculateWindowSizeClass(): WindowSizeClass {
    val width = UIScreen.mainScreen.bounds.useContents { size.width.toInt() }
    val height = UIScreen.mainScreen.bounds.useContents { size.height.toInt() }
    return calculateWindowSizeClass(widthDp = width, heightDp = height)
}
