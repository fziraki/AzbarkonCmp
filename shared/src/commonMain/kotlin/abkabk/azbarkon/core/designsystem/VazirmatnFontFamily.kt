package abkabk.azbarkon.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.vazirmatn_bold
import sarv.shared.generated.resources.vazirmatn_light
import sarv.shared.generated.resources.vazirmatn_regular
import org.jetbrains.compose.resources.Font

@Composable
fun vazirmatnFontFamily(): FontFamily =
    FontFamily(
        Font(Res.font.vazirmatn_regular, FontWeight.Normal),
        Font(Res.font.vazirmatn_bold, FontWeight.Bold),
        Font(Res.font.vazirmatn_light, FontWeight.Light),
    )
