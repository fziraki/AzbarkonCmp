package abkabk.azbarkon.core.designsystem

import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val BASE_WIDTH_DP = 360
private const val SMALL_BASE = 12f
private const val MEDIUM_BASE = 14f
private const val LARGE_BASE = 16f
private const val SMALL_EMPHASIZED_BASE = 10f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun sarvTypography(
    fontSizeScale: Float = 1f,
): Typography {
    val fontFamily = vazirmatnFontFamily()
    val widthDp = LocalWindowSizeClass.current.widthDp
    val heightDp = LocalWindowSizeClass.current.heightDp

    val min = minOf(widthDp, heightDp)

    val scale = floorToHalf((min / BASE_WIDTH_DP).toFloat())
    fun sp(base: Float) = (base * scale * fontSizeScale).sp

    return Typography(
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(SMALL_BASE),
            fontWeight = FontWeight.Bold,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(MEDIUM_BASE),
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(LARGE_BASE),
            fontWeight = FontWeight.Bold,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(SMALL_BASE),
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(MEDIUM_BASE),
            fontWeight = FontWeight.Normal,
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(LARGE_BASE),
            fontWeight = FontWeight.Normal,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(SMALL_BASE),
            fontWeight = FontWeight.Light,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(MEDIUM_BASE),
            fontWeight = FontWeight.Light,
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(LARGE_BASE),
            fontWeight = FontWeight.Light,
        ),
        labelSmallEmphasized = TextStyle(
            fontFamily = fontFamily,
            fontSize = sp(SMALL_EMPHASIZED_BASE),
            fontWeight = FontWeight.Light,
        )
    )
}
