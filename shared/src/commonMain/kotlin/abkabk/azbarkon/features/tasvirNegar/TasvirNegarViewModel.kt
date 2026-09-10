package abkabk.azbarkon.features.tasvirNegar

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.core.uidata.toUiText
import abkabk.azbarkon.core.util.currentTimeMillis
import abkabk.azbarkon.domain.platform.ImageExportService
import abkabk.azbarkon.domain.platform.ShareService
import abkabk.azbarkon.domain.repository.PoemRepository
import abkabk.azbarkon.features.tasvirNegar.model.DividerLayer
import abkabk.azbarkon.features.tasvirNegar.model.EditorBackground
import abkabk.azbarkon.features.tasvirNegar.model.EditorDocument
import abkabk.azbarkon.features.tasvirNegar.model.EditorFontPreset
import abkabk.azbarkon.features.tasvirNegar.model.LayerId
import abkabk.azbarkon.features.tasvirNegar.model.LayerOffset
import abkabk.azbarkon.features.tasvirNegar.model.OptionPanelMode
import abkabk.azbarkon.features.tasvirNegar.model.StickerLayer
import abkabk.azbarkon.features.tasvirNegar.model.TasvirNegarCatalog
import abkabk.azbarkon.features.tasvirNegar.model.TextGravity
import abkabk.azbarkon.features.tasvirNegar.model.TextLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.tasvir_negar_save_failed
import sarv.shared.generated.resources.tasvir_negar_saved
import sarv.shared.generated.resources.tasvir_negar_share_failed
import kotlinx.coroutines.launch

private const val MIN_TEXT_SIZE = 1f
private const val MAX_TEXT_SIZE = 32f

class TasvirNegarViewModel(
    private val poemRepository: PoemRepository,
    private val shareService: ShareService,
    private val imageExportService: ImageExportService,
    private val poemId: Int?,
    private val initialText: String? = null,
) : BaseViewModel<TasvirNegarAction, TasvirNegarState, TasvirNegarEvent>(
        initialState = TasvirNegarState(),
    ) {
    init {
        onAction(TasvirNegarAction.OnLoad)
    }

    override fun onAction(action: TasvirNegarAction) {
        when (action) {
            TasvirNegarAction.OnLoad -> loadInitialDocument()

            TasvirNegarAction.OnBackClick -> emitNavigateBack()
            TasvirNegarAction.OnSaveClick -> emitEvent(TasvirNegarEvent.RequestStoragePermission(forShare = false))
            TasvirNegarAction.OnShareClick -> emitEvent(TasvirNegarEvent.RequestStoragePermission(forShare = true))

            TasvirNegarAction.OnResetCanvas -> resetCanvas()

            TasvirNegarAction.OnEraserClick -> {
                if (state.value.document.selectedLayer != null) {
                    removeSelectedLayer()
                } else {
                    removeBackground()
                }
            }

            TasvirNegarAction.OnToggleEditPanel -> toggleEditPanel()
            TasvirNegarAction.OnShowColorOptions -> showOptionPanel(OptionPanelMode.Color)
            TasvirNegarAction.OnShowShapeOptions -> showOptionPanel(OptionPanelMode.Shape)
            TasvirNegarAction.OnShowFontOptions -> showOptionPanel(OptionPanelMode.Font)
            TasvirNegarAction.OnEnterText -> enterText()
            TasvirNegarAction.OnToggleGrid -> toggleGrid()
            TasvirNegarAction.OnGalleryClick -> requestGalleryPick()

            is TasvirNegarAction.OnLayerSelect -> selectLayer(action.layerId)
            is TasvirNegarAction.OnLayerDrag -> updateLayerOffset(action.layerId, action.offset)
            is TasvirNegarAction.OnSizeProgressChange -> updateSizeProgress(action.progress)
            is TasvirNegarAction.OnColorOptionClick -> handleColorOption(action.index)
            is TasvirNegarAction.OnCustomColorSelected -> applyColor(action.color)
            is TasvirNegarAction.OnShapeOptionClick -> applyShapeOption(action.index)
            is TasvirNegarAction.OnFontOptionClick -> applyFontOption(action.index)
            is TasvirNegarAction.OnTextGravityChange -> updateTextGravity(action.gravity)
            TasvirNegarAction.OnToggleTextBold -> toggleTextBold()
            TasvirNegarAction.OnRemoveSelectedLayer -> removeSelectedLayer()
            is TasvirNegarAction.OnGalleryImagePicked -> applyGalleryImage(action.uri)
            is TasvirNegarAction.OnPoemTextChange -> {
                updateDocument { copy(poemText = poemText.copy(text = action.text)) }
            }
            is TasvirNegarAction.OnPoetNameChange -> {
                updateDocument {
                    copy(poetName = poetName.copy(text = action.text, visible = true))
                }
            }
        }
    }

    fun onExportCompleted(
        imageBytes: ByteArray?,
        forShare: Boolean,
    ) {
        viewModelScope.launch {
            setState { copy(isExporting = false) }
            if (imageBytes == null) {
                val message = UiText.Resource(
                    if (forShare) Res.string.tasvir_negar_share_failed
                    else Res.string.tasvir_negar_save_failed,
                )
                setState {
                    copy(
                        screenState = UiScreenState.Error(message = message),
                    )
                }
                return@launch
            }

            if (forShare) {
                shareService.shareImage(
                    imageBytes = imageBytes,
                    title = state.value.document.poemText.text.lineSequence().firstOrNull(),
                )
            } else {
                val saved =
                    imageExportService.saveToGallery(
                        imageBytes = imageBytes,
                        fileName = "azbarkon_${currentTimeMillis()}.png",
                    )

                val message = UiText.Resource(
                    if (saved) Res.string.tasvir_negar_saved
                    else Res.string.tasvir_negar_save_failed,
                )
                setState {
                    copy(
                        screenState = UiScreenState.Error(message = message, isSuccess = saved),
                    )
                }
            }
        }
    }

    private fun loadInitialDocument() {
        val poemIdValue = poemId
        val initialTextValue = initialText
        if (poemIdValue == null && initialTextValue == null) {
            setState {
                copy(
                    screenState = UiScreenState.Success,
                    document =
                        EditorDocument(
                            poemText = document.poemText.copy(visible = true),
                        ),
                )
            }
            return
        }

        if (poemIdValue == null) {
            setState {
                copy(
                    screenState = UiScreenState.Success,
                    document =
                        EditorDocument(
                            poemText =
                                TextLayer(
                                    text = initialTextValue.orEmpty(),
                                    visible = true,
                                ),
                        ),
                )
            }
            return
        }

        viewModelScope.launch {
            setState { copy(screenState = UiScreenState.Loading) }
            poemRepository
                .getPoemDetail(poemIdValue)
                .onSuccess { detail ->
                    setState {
                        copy(
                            screenState = UiScreenState.Success,
                            document =
                                EditorDocument(
                                    poemText =
                                        TextLayer(
                                            text = initialTextValue ?: detail.verses.joinToString("\n") { it.text },
                                            visible = true,
                                        ),
                                    poetName =
                                        document.poetName.copy(
                                            text = detail.poetName,
                                            visible = detail.poetName.isNotBlank(),
                                        ),
                                ),
                        )
                    }
                }.onFailure { error ->
                    setState { copy(screenState = UiScreenState.Error(error.toUiText())) }
                }
        }
    }

    private fun toggleEditPanel() {
        updateDocument {
            val expanded = !isEditPanelExpanded
            if (expanded) {
                copy(isEditPanelExpanded = true)
            } else {
                copy(
                    isEditPanelExpanded = false,
                    activeOptionPanel = OptionPanelMode.None,
                    selectedLayer = null,
                )
            }
        }
    }

    private fun resetCanvas() {
        updateDocument {
            copy(
                poemText =
                    poemText.copy(
                        offset = LayerOffset(),
                        color = Color.White,
                        gravity = TextGravity.Center,
                        isBold = false,
                        sizeProgress = 1f,
                        visible = poemText.text.isNotBlank(),
                    ),
                poetName =
                    poetName.copy(
                        offset = LayerOffset(y = 120.dp),
                        color = Color.White,
                        sizeProgress = 1f,
                        visible = poetName.text.isNotBlank(),
                    ),
                sticker =
                    StickerLayer().copy(
                        sizeProgress = 1f,
                    ),
                topDivider =
                    DividerLayer().copy(
                        sizeProgress = 1f,
                    ),
                bottomDivider =
                    DividerLayer().copy(
                        sizeProgress = 1f,
                    ),
                background = EditorBackground.SolidColor(Color.Transparent),
                fontPreset = EditorFontPreset.Shekasteh,
                showAlignmentGrid = false,
                selectedLayer = null,
                activeOptionPanel = OptionPanelMode.None,
            )
        }
    }

    private fun showOptionPanel(mode: OptionPanelMode) {
        updateDocument { copy(activeOptionPanel = mode) }
    }

    private fun enterText() {
        updateDocument {
            copy(
                poemText = poemText.copy(visible = true),
                topDivider = topDivider.copy(visible = false, assetId = null),
                bottomDivider = bottomDivider.copy(visible = false, assetId = null),
                selectedLayer = LayerId.PoemText,
            )
        }
    }

    private fun selectLayer(layerId: LayerId?) {
        updateDocument {
            copy(
                selectedLayer = layerId,
                activeOptionPanel = if (layerId == null) OptionPanelMode.None else activeOptionPanel,
            )
        }
    }

    private fun updateLayerOffset(
        layerId: LayerId,
        offset: LayerOffset,
    ) {
        updateDocument {
            when (layerId) {
                LayerId.PoemText -> copy(poemText = poemText.copy(offset = offset))
                LayerId.PoetName -> copy(poetName = poetName.copy(offset = offset))
                LayerId.Sticker -> copy(sticker = sticker.copy(offset = offset))
                LayerId.TopDivider -> copy(topDivider = topDivider.copy(offset = offset))
                LayerId.BottomDivider -> copy(bottomDivider = bottomDivider.copy(offset = offset))
            }
        }
    }

    private fun updateSizeProgress(progress: Float) {
        val value = progress.coerceIn(MIN_TEXT_SIZE, MAX_TEXT_SIZE)
        updateDocument {
            when (selectedLayer) {
                LayerId.PoemText -> copy(poemText = poemText.copy(sizeProgress = value))
                LayerId.PoetName -> copy(poetName = poetName.copy(sizeProgress = value))
                LayerId.Sticker -> copy(sticker = sticker.copy(sizeProgress = value))
                LayerId.TopDivider -> copy(topDivider = topDivider.copy(sizeProgress = value))
                LayerId.BottomDivider -> copy(bottomDivider = bottomDivider.copy(sizeProgress = value))
                null -> this
            }
        }
    }

    private fun handleColorOption(index: Int) {
        val option = TasvirNegarCatalog.colorOptions.getOrNull(index) ?: return
        if (option.isCustomPicker) {
            emitEvent(TasvirNegarEvent.RequestCustomColorPicker)
        } else {
            option.color?.let { applyColor(it) }
        }
    }

    private fun applyColor(color: Color) {
        updateDocument {
            when (selectedLayer) {
                LayerId.Sticker -> copy(sticker = sticker.copy(colorFilter = color))
                LayerId.PoemText -> copy(poemText = poemText.copy(color = color))
                LayerId.PoetName -> copy(poetName = poetName.copy(color = color))
                LayerId.TopDivider -> copy(topDivider = topDivider.copy(colorFilter = color))
                LayerId.BottomDivider -> copy(bottomDivider = bottomDivider.copy(colorFilter = color))
                null -> copy(background = EditorBackground.SolidColor(color))
            }
        }
    }

    private fun applyShapeOption(index: Int) {
        val options = TasvirNegarCatalog.shapeOptions
        if (index !in options.indices) return
        val item = options[index]

        when {
            index == TasvirNegarCatalog.POET_SHAPE_INDEX -> {
                updateDocument {
                    copy(poetName = poetName.copy(visible = true), selectedLayer = LayerId.PoetName)
                }
            }
            index in TasvirNegarCatalog.FIRST_STICKER_INDEX until TasvirNegarCatalog.firstDividerIndex -> {
                updateDocument {
                    copy(
                        sticker = sticker.copy(visible = true, assetId = item.id, galleryUri = null),
                        selectedLayer = LayerId.Sticker,
                    )
                }
            }
            else -> {
                updateDocument {
                    copy(
                        topDivider = topDivider.copy(visible = true, assetId = item.id),
                        bottomDivider = bottomDivider.copy(visible = true, assetId = item.id),
                        selectedLayer = LayerId.TopDivider,
                    )
                }
            }
        }
    }

    private fun applyFontOption(index: Int) {
        val preset =
            when (index) {
                0 -> EditorFontPreset.Shekasteh
                1 -> EditorFontPreset.Yekan
                else -> EditorFontPreset.Tanha
            }
        updateDocument { copy(fontPreset = preset) }
    }

    private fun updateTextGravity(gravity: TextGravity) {
        updateDocument { copy(poemText = poemText.copy(gravity = gravity)) }
    }

    private fun toggleTextBold() {
        updateDocument { copy(poemText = poemText.copy(isBold = !poemText.isBold)) }
    }

    private fun removeSelectedLayer() {
        updateDocument {
            when (selectedLayer) {
                LayerId.PoemText ->
                    copy(
                        poemText = poemText.copy(visible = false),
                        topDivider = topDivider.copy(visible = false),
                        bottomDivider = bottomDivider.copy(visible = false),
                        selectedLayer = null,
                    )
                LayerId.PoetName -> copy(poetName = poetName.copy(visible = false), selectedLayer = null)
                LayerId.Sticker -> copy(sticker = sticker.copy(visible = false), selectedLayer = null)
                LayerId.TopDivider -> copy(topDivider = topDivider.copy(visible = false), selectedLayer = null)
                LayerId.BottomDivider -> copy(bottomDivider = bottomDivider.copy(visible = false), selectedLayer = null)
                null -> this
            }
        }
    }

    private fun removeBackground() {
        updateDocument {
            copy(background = EditorBackground.SolidColor(Color.Transparent))
        }
    }

    private fun toggleGrid() {
        updateDocument { copy(showAlignmentGrid = !showAlignmentGrid) }
    }

    private fun requestGalleryPick() {
        updateDocument {
            copy(activeOptionPanel = OptionPanelMode.None)
        }
        emitEvent(TasvirNegarEvent.RequestGalleryPick)
    }

    private fun applyGalleryImage(uri: String?) {
        if (uri.isNullOrBlank()) return
        updateDocument {
            if (selectedLayer == LayerId.Sticker) {
                copy(sticker = sticker.copy(galleryUri = uri, assetId = null, visible = true))
            } else {
                copy(background = EditorBackground.GalleryImage(uri))
            }
        }
    }

    internal fun requestExport(forShare: Boolean) {
        updateDocument {
            copy(
                selectedLayer = null,
                isEditPanelExpanded = false,
                activeOptionPanel = OptionPanelMode.None,
                showAlignmentGrid = false,
            )
        }
        setState { copy(isExporting = true, exportForShare = forShare) }
    }

    private fun updateDocument(reducer: EditorDocument.() -> EditorDocument) {
        setState { copy(document = document.reducer()) }
    }

    private fun emitNavigateBack() {
        viewModelScope.launch { sendEvent(TasvirNegarEvent.NavigateBack) }
    }

    private fun emitEvent(event: TasvirNegarEvent) {
        viewModelScope.launch { sendEvent(event) }
    }
}
