package abkabk.azbarkon.features.tasvirNegar.components

import abkabk.azbarkon.features.tasvirNegar.model.CatalogItem
import abkabk.azbarkon.features.tasvirNegar.model.ColorOption
import abkabk.azbarkon.features.tasvirNegar.model.EditorFontPreset
import abkabk.azbarkon.features.tasvirNegar.model.OptionPanelMode
import abkabk.azbarkon.features.tasvirNegar.model.TasvirNegarCatalog
import abkabk.azbarkon.features.tasvirNegar.util.tasvirNegarPainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.add_box_24px
import sarv.shared.generated.resources.tasvir_font_1
import sarv.shared.generated.resources.tasvir_font_2
import sarv.shared.generated.resources.tasvir_font_3
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun OptionsRow(
    mode: OptionPanelMode,
    onColorClick: (Int) -> Unit,
    onShapeClick: (Int) -> Unit,
    onFontClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    if (mode == OptionPanelMode.None) return

    Box(
        modifier =
            modifier
                .then(if (isExpanded) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        when (mode) {
            OptionPanelMode.Color -> ColorOptionsRow(onColorClick = onColorClick, isExpanded = isExpanded)
            OptionPanelMode.Shape -> ShapeOptionsRow(onShapeClick = onShapeClick, isExpanded = isExpanded)
            OptionPanelMode.Font -> FontOptionsRow(onFontClick = onFontClick, isExpanded = isExpanded)
            OptionPanelMode.None -> Unit
        }
    }
}

@Composable
private fun ColorOptionsRow(
    onColorClick: (Int) -> Unit,
    isExpanded: Boolean = false,
) {
    if (isExpanded) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = LocalSarvDimensions.current.dimen8,
                    vertical = LocalSarvDimensions.current.dimen8,
                ),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            itemsIndexed(TasvirNegarCatalog.colorOptions) { index, option ->
                ColorOptionItem(option = option, onClick = { onColorClick(index) })
            }
        }
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = LocalSarvDimensions.current.dimen8,
                    vertical = LocalSarvDimensions.current.dimen8,
                ),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            itemsIndexed(TasvirNegarCatalog.colorOptions) { index, option ->
                ColorOptionItem(option = option, onClick = { onColorClick(index) })
            }
        }
    }
}

@Composable
private fun ColorOptionItem(
    option: ColorOption,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(LocalSarvDimensions.current.dimen40)
                .clip(CircleShape)
                .border(LocalSarvDimensions.current.dimen1, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .then(
                    if (option.color != null) {
                        Modifier.background(option.color)
                    } else if (!option.isCustomPicker) {
                        Modifier.background(MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        // custom picker: outline only, no fill
                        Modifier
                    },
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (option.isCustomPicker) {
            Icon(
                modifier = Modifier.size(LocalSarvDimensions.current.dimen36),
                painter = painterResource(Res.drawable.add_box_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShapeOptionsRow(
    onShapeClick: (Int) -> Unit,
    isExpanded: Boolean = false,
) {
    if (isExpanded) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().padding(vertical = LocalSarvDimensions.current.dimen8),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            itemsIndexed(TasvirNegarCatalog.shapeOptions) { index, item ->
                ShapeOptionItem(item = item, onClick = { onShapeClick(index) })
            }
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = LocalSarvDimensions.current.dimen8),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            itemsIndexed(TasvirNegarCatalog.shapeOptions) { index, item ->
                ShapeOptionItem(item = item, onClick = { onShapeClick(index) })
            }
        }
    }
}

@Composable
private fun ShapeOptionItem(
    item: CatalogItem,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(LocalSarvDimensions.current.dimen56)
                .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    LocalSarvDimensions.current.dimen1,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(LocalSarvDimensions.current.dimen8),
                )
                .clickable(onClick = onClick)
                .padding(LocalSarvDimensions.current.dimen8),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = tasvirNegarPainter(item.id),
            contentDescription = null,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen40),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun FontOptionsRow(
    onFontClick: (Int) -> Unit,
    isExpanded: Boolean = false,
) {
    val labels =
        listOf(
            stringResource(Res.string.tasvir_font_1) to EditorFontPreset.Shekasteh,
            stringResource(Res.string.tasvir_font_2) to EditorFontPreset.Yekan,
            stringResource(Res.string.tasvir_font_3) to EditorFontPreset.Tanha,
        )
    if (isExpanded) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = LocalSarvDimensions.current.dimen8),
            verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            labels.forEachIndexed { index, (label, preset) ->
                Box(
                    modifier =
                        Modifier
                            .size(LocalSarvDimensions.current.dimen56)
                            .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                LocalSarvDimensions.current.dimen1,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(LocalSarvDimensions.current.dimen8),
                            )
                            .clickable { onFontClick(index) }
                            .padding(LocalSarvDimensions.current.dimen8),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = editorFontFamily(preset)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = LocalSarvDimensions.current.dimen8),
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen12),
        ) {
            labels.forEachIndexed { index, (label, preset) ->
                Box(
                    modifier =
                        Modifier
                            .size(LocalSarvDimensions.current.dimen56)
                            .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen8))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                LocalSarvDimensions.current.dimen1,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(LocalSarvDimensions.current.dimen8),
                            )
                            .clickable { onFontClick(index) }
                            .padding(LocalSarvDimensions.current.dimen8),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = editorFontFamily(preset)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
