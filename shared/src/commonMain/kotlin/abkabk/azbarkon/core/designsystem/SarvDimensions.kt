@file:Suppress("MagicNumber", "LongParameterList")

package abkabk.azbarkon.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

private const val BASE_WIDTH_DP = 360

@Immutable
class SarvDimensions(
    val dimen1: Dp = 1.dp,
    val dimen2: Dp = 2.dp,
    val dimen4: Dp = 4.dp,
    val dimen6: Dp = 6.dp,
    val dimen8: Dp = 8.dp,
    val dimen10: Dp = 10.dp,
    val dimen12: Dp = 12.dp,
    val dimen16: Dp = 16.dp,
    val dimen20: Dp = 20.dp,
    val dimen22: Dp = 22.dp,
    val dimen24: Dp = 24.dp,
    val dimen28: Dp = 28.dp,
    val dimen32: Dp = 32.dp,
    val dimen36: Dp = 36.dp,
    val dimen40: Dp = 40.dp,
    val dimen48: Dp = 48.dp,
    val dimen56: Dp = 56.dp,
    val dimen64: Dp = 64.dp,
    val dimen72: Dp = 72.dp,
    val dimen80: Dp = 80.dp,
    val dimen88: Dp = 88.dp,
    val dimen92: Dp = 92.dp,
    val dimen96: Dp = 96.dp,
    val dimen128: Dp = 128.dp,
)

val LocalSarvDimensions = staticCompositionLocalOf {
    SarvDimensions()
}


fun floorToHalf(value: Float): Float {
    return (floor(value * 2) / 2)
}
@Composable
fun sarvDimensions(widthDp: Int, heightDp: Int): SarvDimensions {

    println("width $widthDp")

    val min = minOf(widthDp, heightDp)

    val scale = floorToHalf((min / BASE_WIDTH_DP).toFloat())
    fun dp(base: Int) = (base * scale).dp

    return SarvDimensions(
        dimen1 = dp(1),
        dimen2 = dp(2),
        dimen4 = dp(4),
        dimen6 = dp(6),
        dimen8 = dp(8),
        dimen10 = dp(10),
        dimen12 = dp(12),
        dimen16 = dp(16),
        dimen20 = dp(20),
        dimen22 = dp(22),
        dimen24 = dp(24),
        dimen28 = dp(28),
        dimen32 = dp(32),
        dimen36 = dp(36),
        dimen40 = dp(40),
        dimen48 = dp(48),
        dimen56 = dp(56),
        dimen64 = dp(64),
        dimen72 = dp(72),
        dimen80 = dp(80),
        dimen88 = dp(88),
        dimen92 = dp(92),
        dimen96 = dp(96),
        dimen128 = dp(128),
    )
}
