package abkabk.azbarkon.features.tasvirNegar.components

import abkabk.azbarkon.core.designsystem.secondary
import abkabk.azbarkon.features.tasvirNegar.model.DividerLayer
import abkabk.azbarkon.features.tasvirNegar.model.EditorBackground
import abkabk.azbarkon.features.tasvirNegar.model.EditorDocument
import abkabk.azbarkon.features.tasvirNegar.model.LayerId
import abkabk.azbarkon.features.tasvirNegar.model.LayerOffset
import abkabk.azbarkon.features.tasvirNegar.model.TextGravity
import abkabk.azbarkon.features.tasvirNegar.util.LocalGalleryImage
import abkabk.azbarkon.features.tasvirNegar.util.rememberCanvasCaptureModifier
import abkabk.azbarkon.features.tasvirNegar.util.tasvirNegarPainter
import abkabk.azbarkon.features.tasvirNegar.util.textAlignForGravity
import abkabk.azbarkon.features.tasvirNegar.util.textDirectionFor
import abkabk.azbarkon.features.tasvirNegar.util.textLayoutDirectionFor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.ic_align_center
import sarv.shared.generated.resources.ic_align_left
import sarv.shared.generated.resources.ic_align_right
import sarv.shared.generated.resources.ic_bold
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions

private const val MIN_LAYER_WIDTH_FRACTION = 0.2f
private const val MAX_LAYER_WIDTH_FRACTION = 0.95f
private const val STICKER_SIZE_SCALE_FACTOR = 4
private const val GRID_THIRDS = 3f
private const val MIN_SOLID_COLOR_ALPHA = 0.05f
private const val LUMINANCE_CONTRAST_THRESHOLD = 0.5f

data class EditorCallbacks(
    val onLayerSelect: (LayerId?) -> Unit,
    val onLayerDrag: (LayerId, LayerOffset) -> Unit,
    val onPoemTextChange: (String) -> Unit,
    val onPoetNameChange: (String) -> Unit,
    val onTextGravityChange: (TextGravity) -> Unit,
    val onToggleTextBold: () -> Unit,
    val onCaptureReady: (suspend () -> ByteArray?) -> Unit,
)

private data class SelectionPalette(
    val main: Color,
    val halo: Color?,
)

// ponytail: image backgrounds have no known pixel color, so the halo (white ring /
// offset line) keeps overlays visible on any image. Pixel sampling is the upgrade path.
private fun selectionPalette(
    background: EditorBackground,
    fallbackColor: Color,
): SelectionPalette {
    val isImage =
        background is EditorBackground.GalleryImage || background is EditorBackground.CatalogTexture
    val solidColor =
        (background as? EditorBackground.SolidColor)
            ?.takeIf { it.color.alpha > MIN_SOLID_COLOR_ALPHA }
            ?.let { lerp(it.color, fallbackColor, 1f - it.color.alpha) }
    val contrastBase = solidColor ?: fallbackColor
    return if (isImage) {
        SelectionPalette(Color.Black.copy(alpha = 0.45f), Color.White.copy(alpha = 0.45f))
    } else {
        val contrast = if (contrastBase.luminance() > LUMINANCE_CONTRAST_THRESHOLD) Color.Black else Color.White
        SelectionPalette(contrast.copy(alpha = 0.5f), null)
    }
}

@Composable
private fun selectionBorderModifier(palette: SelectionPalette): Modifier =
    (palette.halo?.let { Modifier.border(LocalSarvDimensions.current.dimen2, it) } ?: Modifier)
        .border(LocalSarvDimensions.current.dimen4, palette.main)

data class DraggableLayerCallbacks(
    val onSelect: () -> Unit,
    val onDrag: (LayerOffset) -> Unit,
    val onTextGravityChange: ((TextGravity) -> Unit)? = null,
    val onToggleBold: (() -> Unit)? = null,
)

private data class TextFormattingControls(
    val visible: Boolean = false,
    val isBold: Boolean = false,
)

@Composable
fun EditorCanvas(
    document: EditorDocument,
    callbacks: EditorCallbacks,
    modifier: Modifier = Modifier,
    showEditOverlays: Boolean = true,
) {
    val captureModifier = rememberCanvasCaptureModifier(onCaptureReady = callbacks.onCaptureReady)
    val fontFamily = editorFontFamily(document.fontPreset)
    val fallbackColor = MaterialTheme.colorScheme.background
    val palette =
        remember(document.background, fallbackColor) {
            selectionPalette(document.background, fallbackColor)
        }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val canvasSize = minOf(maxWidth, maxHeight)
        val minLayerWidth = canvasSize * MIN_LAYER_WIDTH_FRACTION
        val maxLayerWidth = canvasSize * MAX_LAYER_WIDTH_FRACTION
        val isPoemEditing =
            showEditOverlays &&
                document.isEditPanelExpanded &&
                document.selectedLayer == LayerId.PoemText
        val isPoetEditing =
            showEditOverlays &&
                document.isEditPanelExpanded &&
                document.selectedLayer == LayerId.PoetName

        Box(
            modifier =
                Modifier
                    .size(canvasSize)
                    .then(captureModifier)
                    .clipToBounds(),
        ) {
            // Canvas uses physical LTR coordinates so pan/drag matches finger movement on RTL screens.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                CanvasBackground(document = document)

                if (document.showAlignmentGrid && showEditOverlays) {
                    AlignmentGrid(
                        palette = palette,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                if (showEditOverlays && document.selectedLayer == null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .then(selectionBorderModifier(palette)),
                    )
                }

                if (document.poemText.visible) {
                    PoemTextLayer(
                        document = document,
                        callbacks = callbacks,
                        fontFamily = fontFamily,
                        minLayerWidth = minLayerWidth,
                        maxLayerWidth = maxLayerWidth,
                        isPoemEditing = isPoemEditing,
                        showEditOverlays = showEditOverlays,
                        selectionPalette = palette,
                    )
                }

                if (document.topDivider.visible && document.topDivider.assetId != null) {
                    CanvasDividerLayer(
                        divider = document.topDivider,
                        layerId = LayerId.TopDivider,
                        selected = document.selectedLayer == LayerId.TopDivider,
                        callbacks = callbacks,
                        minLayerWidth = minLayerWidth,
                        showEditOverlays = showEditOverlays,
                        flipVertical = false,
                        selectionPalette = palette,
                    )
                }

                if (document.bottomDivider.visible && document.bottomDivider.assetId != null) {
                    CanvasDividerLayer(
                        divider = document.bottomDivider,
                        layerId = LayerId.BottomDivider,
                        selected = document.selectedLayer == LayerId.BottomDivider,
                        callbacks = callbacks,
                        minLayerWidth = minLayerWidth,
                        showEditOverlays = showEditOverlays,
                        flipVertical = true,
                        selectionPalette = palette,
                    )
                }

                if (document.poetName.visible) {
                    PoetNameLayer(
                        document = document,
                        callbacks = callbacks,
                        fontFamily = fontFamily,
                        minLayerWidth = minLayerWidth,
                        maxLayerWidth = maxLayerWidth,
                        isPoetEditing = isPoetEditing,
                        showEditOverlays = showEditOverlays,
                        selectionPalette = palette,
                    )
                }

                if (document.sticker.visible) {
                    StickerLayer(
                        document = document,
                        callbacks = callbacks,
                        minLayerWidth = minLayerWidth,
                        showEditOverlays = showEditOverlays,
                        selectionPalette = palette,
                    )
                }
            }
        }
    }
}

@Composable
private fun PoemTextLayer(
    document: EditorDocument,
    callbacks: EditorCallbacks,
    fontFamily: FontFamily,
    minLayerWidth: Dp,
    maxLayerWidth: Dp,
    isPoemEditing: Boolean,
    showEditOverlays: Boolean,
    selectionPalette: SelectionPalette,
) {
    DraggableLayer(
        offset = document.poemText.offset,
        selected = document.selectedLayer == LayerId.PoemText,
        showLayerControls = showEditOverlays,
        selectionPalette = selectionPalette,
        callbacks =
            DraggableLayerCallbacks(
                onSelect = { callbacks.onLayerSelect(LayerId.PoemText) },
                onDrag = { callbacks.onLayerDrag(LayerId.PoemText, it) },
                onTextGravityChange = callbacks.onTextGravityChange,
                onToggleBold = callbacks.onToggleTextBold,
            ),
        textFormatting =
            TextFormattingControls(
                visible = showEditOverlays && document.selectedLayer == LayerId.PoemText,
                isBold = document.poemText.isBold,
            ),
        minWidth = minLayerWidth,
    ) {
        DirectionalTextField(
            value = document.poemText.text,            onValueChange = callbacks.onPoemTextChange,
            readOnly = !isPoemEditing,
            textStyle =
                TextStyle(
                    color = document.poemText.color,
                    fontSize = (document.poemText.baseFontSizeSp + document.poemText.sizeProgress).sp,
                    fontWeight =
                        if (document.poemText.isBold) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    textAlign = textAlignForGravity(document.poemText.gravity),
                    fontFamily = fontFamily,
                ),
            modifier =
                Modifier
                    .widthIn(min = minLayerWidth, max = maxLayerWidth)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun PoetNameLayer(
    document: EditorDocument,
    callbacks: EditorCallbacks,
    fontFamily: FontFamily,
    minLayerWidth: Dp,
    maxLayerWidth: Dp,
    isPoetEditing: Boolean,
    showEditOverlays: Boolean,
    selectionPalette: SelectionPalette,
) {
    DraggableLayer(
        offset = document.poetName.offset,
        selected = document.selectedLayer == LayerId.PoetName,
        showLayerControls = showEditOverlays,
        selectionPalette = selectionPalette,
        callbacks =
            DraggableLayerCallbacks(
                onSelect = { callbacks.onLayerSelect(LayerId.PoetName) },
                onDrag = { callbacks.onLayerDrag(LayerId.PoetName, it) },
            ),
        alignment = Alignment.Center,
        minWidth = minLayerWidth,
    ) {
        DirectionalTextField(
            value = document.poetName.text,
            onValueChange = callbacks.onPoetNameChange,
            readOnly = !isPoetEditing,
            textStyle =
                TextStyle(
                    color = document.poetName.color,
                    fontSize = (document.poetName.baseFontSizeSp + document.poetName.sizeProgress).sp,
                    textAlign = TextAlign.Center,
                    fontFamily = fontFamily,
                ),
            modifier =
                Modifier
                    .widthIn(min = minLayerWidth, max = maxLayerWidth)
                    .wrapContentWidth(align = Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun CanvasDividerLayer(
    divider: DividerLayer,
    layerId: LayerId,
    selected: Boolean,
    callbacks: EditorCallbacks,
    minLayerWidth: Dp,
    showEditOverlays: Boolean,
    flipVertical: Boolean,
    selectionPalette: SelectionPalette,
) {
    DraggableLayer(
        offset = divider.offset,
        selected = selected,
        showLayerControls = showEditOverlays,
        selectionPalette = selectionPalette,
        callbacks =
            DraggableLayerCallbacks(
                onSelect = { callbacks.onLayerSelect(layerId) },
                onDrag = { callbacks.onLayerDrag(layerId, it) },
            ),
        alignment = if (layerId == LayerId.TopDivider) Alignment.TopCenter else Alignment.BottomCenter,
        minWidth = minLayerWidth,
    ) {
        DividerImage(
            assetId = divider.assetId.orEmpty(),
            width = divider.baseWidthDp + (divider.sizeProgress * 4).dp,
            colorFilter = divider.colorFilter,
            flipVertical = flipVertical,
        )
    }
}

@Composable
private fun StickerLayer(
    document: EditorDocument,
    callbacks: EditorCallbacks,
    minLayerWidth: Dp,
    showEditOverlays: Boolean,
    selectionPalette: SelectionPalette,
) {
    DraggableLayer(
        offset = document.sticker.offset,
        selected = document.selectedLayer == LayerId.Sticker,
        showLayerControls = showEditOverlays,
        selectionPalette = selectionPalette,
        callbacks =
            DraggableLayerCallbacks(
                onSelect = { callbacks.onLayerSelect(LayerId.Sticker) },
                onDrag = { callbacks.onLayerDrag(LayerId.Sticker, it) },
            ),
        minWidth = minLayerWidth,
    ) {
        StickerContent(document = document)
    }
}

@Composable
private fun DirectionalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    val layoutDirection = textLayoutDirectionFor(value)
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            textStyle = textStyle.copy(textDirection = textDirectionFor(value)),
            modifier = modifier,
        )
    }
}

@Composable
private fun DraggableLayer(
    offset: LayerOffset,
    selected: Boolean,
    showLayerControls: Boolean,
    callbacks: DraggableLayerCallbacks,
    minWidth: Dp,
    selectionPalette: SelectionPalette,
    alignment: Alignment = Alignment.Center,
    textFormatting: TextFormattingControls = TextFormattingControls(),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var dragDeltaPx by remember { mutableStateOf(Offset.Zero) }
    val currentOffset by rememberUpdatedState(offset)
    val currentOnDrag by rememberUpdatedState(callbacks.onDrag)
    val displaySelected = selected && showLayerControls

    LaunchedEffect(offset) {
        dragDeltaPx = Offset.Zero
    }

    val displayOffset =
        LayerOffset(
            x = offset.x + with(density) { dragDeltaPx.x.toDp() },
            y = offset.y + with(density) { dragDeltaPx.y.toDp() },
        )

    fun commitDrag() {
        if (dragDeltaPx != Offset.Zero) {
            currentOnDrag(
                LayerOffset(
                    x = currentOffset.x + with(density) { dragDeltaPx.x.toDp() },
                    y = currentOffset.y + with(density) { dragDeltaPx.y.toDp() },
                ),
            )
        }
        dragDeltaPx = Offset.Zero
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = alignment,
    ) {
        Column(
            modifier =
                Modifier
                    .graphicsLayer {
                        translationX = displayOffset.x.toPx()
                        translationY = displayOffset.y.toPx()
                    }
                    .wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .widthIn(min = minWidth)
                        .wrapContentWidth()
                        .padding(LocalSarvDimensions.current.dimen4)
                        .then(if (displaySelected) selectionBorderModifier(selectionPalette) else Modifier)
                        .padding(LocalSarvDimensions.current.dimen8)
                        .then(
                            if (showLayerControls) {
                                Modifier
                                    .pointerInput(Unit) {
                                        // Initial pass fires before the text field consumes the
                                        // down event, so tapping the text selects the layer while
                                        // cursor/focus behavior stays untouched (nothing consumed).
                                        awaitEachGesture {
                                            awaitFirstDown(
                                                pass = PointerEventPass.Initial,
                                                requireUnconsumed = false,
                                            )
                                            if (waitForUpOrCancellation(pass = PointerEventPass.Initial) != null) {
                                                callbacks.onSelect()
                                            }
                                        }
                                    }.pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                callbacks.onSelect()
                                                dragDeltaPx = Offset.Zero
                                            },
                                            onDragEnd = { commitDrag() },
                                            onDragCancel = { dragDeltaPx = Offset.Zero },
                                        ) { change, dragAmount ->
                                            change.consume()
                                            dragDeltaPx += dragAmount
                                        }
                                    }
                            } else {
                                Modifier
                            },
                        ),
            ) {
                content()
            }

            if (displaySelected) {
                val showFormattingControls =
                    textFormatting.visible && callbacks.onTextGravityChange != null && callbacks.onToggleBold != null
                if (showFormattingControls) {
                    TextFormattingBar(
                        isBold = textFormatting.isBold,
                        onTextGravityChange = callbacks.onTextGravityChange,
                        onToggleBold = callbacks.onToggleBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun TextFormattingBar(
    isBold: Boolean,
    onTextGravityChange: (TextGravity) -> Unit,
    onToggleBold: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(top = LocalSarvDimensions.current.dimen4),
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen2, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormattingIcon(
            drawable = Res.drawable.ic_bold,
            tint = if (isBold) secondary else Color.White,
            onClick = onToggleBold,
        )
        FormattingIcon(Res.drawable.ic_align_left) { onTextGravityChange(TextGravity.Start) }
        FormattingIcon(Res.drawable.ic_align_center) { onTextGravityChange(TextGravity.Center) }
        FormattingIcon(Res.drawable.ic_align_right) { onTextGravityChange(TextGravity.End) }
    }
}

@Composable
private fun FormattingIcon(
    drawable: DrawableResource,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(LocalSarvDimensions.current.dimen28)) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
        )
    }
}

@Composable
private fun StickerContent(document: EditorDocument) {
    val sticker = document.sticker
    val sizeModifier = Modifier.size(sticker.baseSizeDp + (sticker.sizeProgress * STICKER_SIZE_SCALE_FACTOR).dp)
    val colorFilter = sticker.colorFilter?.let { ColorFilter.tint(it) }

    when {
        sticker.galleryUri != null -> {
            LocalGalleryImage(
                uri = sticker.galleryUri,
                modifier = sizeModifier,
                contentScale = ContentScale.Fit,
            )
        }
        sticker.assetId != null -> {
            Image(
                painter = tasvirNegarPainter(sticker.assetId),
                contentDescription = null,
                modifier = sizeModifier,
                colorFilter = colorFilter,
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun CanvasBackground(
    document: EditorDocument,
) {
    when (val background = document.background) {
        is EditorBackground.None -> {
            Box(modifier = Modifier.fillMaxSize())
        }
        is EditorBackground.SolidColor -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(background.color),
            )
        }
        is EditorBackground.GalleryImage -> {
            LocalGalleryImage(
                uri = background.uri,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        is EditorBackground.CatalogTexture -> {
            Image(
                painter = tasvirNegarPainter(background.drawableName),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun DividerImage(
    assetId: String,
    width: Dp,
    colorFilter: Color?,
    flipVertical: Boolean,
) {
    Image(
        painter = tasvirNegarPainter(assetId),
        contentDescription = null,
        modifier =
            Modifier
                .width(width)
                .then(
                    if (flipVertical) {
                        Modifier.graphicsLayer(scaleY = -1f)
                    } else {
                        Modifier
                    },
                ),
        colorFilter = colorFilter?.let { ColorFilter.tint(it) },
        contentScale = ContentScale.FillWidth,
    )
}

@Composable
private fun AlignmentGrid(
    palette: SelectionPalette,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val thirdW = size.width / GRID_THIRDS
        val thirdH = size.height / GRID_THIRDS
        for (i in 1..2) {
            val x = thirdW * i
            val y = thirdH * i
            palette.halo?.let {
                drawLine(it, Offset(x - 1f, 0f), Offset(x - 1f, size.height), strokeWidth = 1f)
                drawLine(it, Offset(0f, y - 1f), Offset(size.width, y - 1f), strokeWidth = 1f)
            }
            drawLine(palette.main, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            drawLine(palette.main, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
    }
}
