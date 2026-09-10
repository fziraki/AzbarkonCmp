package abkabk.azbarkon.features.tasvirNegar.components

import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.designsystem.secondary
import abkabk.azbarkon.core.designsystem.surfaceVariant
import abkabk.azbarkon.features.tasvirNegar.TasvirNegarAction
import abkabk.azbarkon.ui.components.SarvSlider
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.arrow_back_right
import sarv.shared.generated.resources.cd_back
import sarv.shared.generated.resources.download
import sarv.shared.generated.resources.ic_color
import sarv.shared.generated.resources.ic_delete
import sarv.shared.generated.resources.ic_edit
import sarv.shared.generated.resources.ic_grid
import sarv.shared.generated.resources.ic_sticker
import sarv.shared.generated.resources.ic_text_format
import sarv.shared.generated.resources.ic_texture
import sarv.shared.generated.resources.ic_wallpaper
import sarv.shared.generated.resources.reset_image
import sarv.shared.generated.resources.share
import sarv.shared.generated.resources.tasvir_color
import sarv.shared.generated.resources.tasvir_edit
import sarv.shared.generated.resources.tasvir_eraser
import sarv.shared.generated.resources.tasvir_font
import sarv.shared.generated.resources.tasvir_gallery
import sarv.shared.generated.resources.tasvir_grid
import sarv.shared.generated.resources.tasvir_negar_save
import sarv.shared.generated.resources.tasvir_share
import sarv.shared.generated.resources.tasvir_sticker
import sarv.shared.generated.resources.tasvir_text
import sarv.shared.generated.resources.tasvir_texture
import sarv.shared.generated.resources.text_fields

private const val MIN_TEXT_SIZE = 1f
private const val MAX_TEXT_SIZE = 32f
private const val PREVIEW_PROGRESS = 22f

@Composable
fun EditorHeader(
    onResetClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(LocalSarvDimensions.current.dimen56)
                .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(Res.drawable.arrow_back_right),
                contentDescription = stringResource(Res.string.cd_back),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            )
        }

        Box(modifier = Modifier.weight(1f))

        IconButton(onClick = onResetClick) {
            Icon(
                painter = painterResource(Res.drawable.reset_image),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LabeledIconButton(
    drawable: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4)
    ) {
        Icon(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            painter = painterResource(drawable),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmallEmphasized,
        )
    }
}

@Composable
fun EditorFooter(
    onEraserClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(LocalSarvDimensions.current.dimen72)
                .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        LabeledIconButton(
            drawable = Res.drawable.download,
            label = stringResource(Res.string.tasvir_negar_save),
            onClick = onDownloadClick,
        )
        LabeledIconButton(
            drawable = Res.drawable.ic_delete,
            label = stringResource(Res.string.tasvir_eraser),
            onClick = onEraserClick,
        )
        LabeledIconButton(
            drawable = Res.drawable.ic_edit,
            label = stringResource(Res.string.tasvir_edit),
            onClick = onEditClick,
        )
        LabeledIconButton(
            drawable = Res.drawable.share,
            label = stringResource(Res.string.tasvir_share),
            onClick = onShareClick,
        )
    }
}

@Composable
fun EditToolbar(
    onAction: (TasvirNegarAction) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    if (isExpanded) {
        LazyColumn(
            modifier =
                modifier
                    .fillMaxHeight()
                    .width(LocalSarvDimensions.current.dimen72)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalArrangement = Arrangement.spacedBy(
                space = LocalSarvDimensions.current.dimen16,
                alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(LocalSarvDimensions.current.dimen4)
        ) {
            item {
            ToolbarIcon(Res.drawable.ic_text_format, tint, stringResource(Res.string.tasvir_font)) {
                onAction(TasvirNegarAction.OnShowFontOptions)
            }}
            item {
            ToolbarIcon(Res.drawable.text_fields, tint, stringResource(Res.string.tasvir_text)) {
                onAction(TasvirNegarAction.OnEnterText)
            }}
            item {
            ToolbarIcon(Res.drawable.ic_sticker, tint, stringResource(Res.string.tasvir_sticker)) {
                onAction(TasvirNegarAction.OnShowShapeOptions)
            }}
            item {
            ToolbarIcon(Res.drawable.ic_color, tint, stringResource(Res.string.tasvir_color)) {
                onAction(TasvirNegarAction.OnShowColorOptions)
            }}
            item {
            ToolbarIcon(Res.drawable.ic_texture, tint, stringResource(Res.string.tasvir_texture)) {
                onAction(TasvirNegarAction.OnLayerSelect(null))
            }}
            item {
            ToolbarIcon(Res.drawable.ic_grid, tint, stringResource(Res.string.tasvir_grid)) {
                onAction(TasvirNegarAction.OnToggleGrid)
            }}
            item {
            ToolbarIcon(Res.drawable.ic_wallpaper, tint, stringResource(Res.string.tasvir_gallery)) {
                onAction(TasvirNegarAction.OnGalleryClick)
            }}
        }
    } else {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(LocalSarvDimensions.current.dimen72)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ToolbarIcon(Res.drawable.ic_text_format, tint, stringResource(Res.string.tasvir_font)) {
                onAction(TasvirNegarAction.OnShowFontOptions)
            }
            ToolbarIcon(Res.drawable.text_fields, tint, stringResource(Res.string.tasvir_text)) {
                onAction(TasvirNegarAction.OnEnterText)
            }
            ToolbarIcon(Res.drawable.ic_sticker, tint, stringResource(Res.string.tasvir_sticker)) {
                onAction(TasvirNegarAction.OnShowShapeOptions)
            }
            ToolbarIcon(Res.drawable.ic_color, tint, stringResource(Res.string.tasvir_color)) {
                onAction(TasvirNegarAction.OnShowColorOptions)
            }
            ToolbarIcon(Res.drawable.ic_texture, tint, stringResource(Res.string.tasvir_texture)) {
                onAction(TasvirNegarAction.OnLayerSelect(null))
            }
            ToolbarIcon(Res.drawable.ic_grid, tint, stringResource(Res.string.tasvir_grid)) {
                onAction(TasvirNegarAction.OnToggleGrid)
            }
            ToolbarIcon(Res.drawable.ic_wallpaper, tint, stringResource(Res.string.tasvir_gallery)) {
                onAction(TasvirNegarAction.OnGalleryClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ToolbarIcon(
    drawable: org.jetbrains.compose.resources.DrawableResource,
    tint: Color,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.wrapContentSize()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen2)
    ) {
        Icon(
            modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
            painter = painterResource(drawable),
            contentDescription = label,
            tint = tint,
        )
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmallEmphasized,
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VerticalSizeSlider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val valueRange = MIN_TEXT_SIZE..MAX_TEXT_SIZE
    val sliderValue = progress.coerceIn(valueRange.start, valueRange.endInclusive)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = sliderValue.toInt().toString(),
            style = MaterialTheme.typography.labelSmallEmphasized.copy(
                color = Color.White,
                shadow = Shadow(Color.Black.copy(alpha = 0.6f), offset = Offset(0f, 1f), blurRadius = 2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            ),
        )

        SarvSlider(
            value = sliderValue,
            onValueChange = onProgressChange,
            valueRange = valueRange,
            vertical = true,
            activeTrackColor = secondary,
            inactiveTrackColor = Color.Gray.copy(alpha = 0.25f),
            thumbColor = surfaceVariant,
        )
    }
}

@Preview
@Composable
private fun VerticalSizeSliderPreview() {
    SarvTheme {
        var progress by remember { mutableFloatStateOf(PREVIEW_PROGRESS) }
        VerticalSizeSlider(
            progress = progress,
            onProgressChange = { progress = it },
        )
    }
}
