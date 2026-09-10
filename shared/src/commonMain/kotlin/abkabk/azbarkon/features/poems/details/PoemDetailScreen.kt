package abkabk.azbarkon.features.poems.details

import abkabk.azbarkon.core.ui.FindTextField
import abkabk.azbarkon.core.ui.keyboardAboveIme
import abkabk.azbarkon.core.uidata.BaseScreen
import abkabk.azbarkon.core.uidata.LocalSnackbarHostState
import abkabk.azbarkon.ui.components.SarvSnackbarHost
import abkabk.azbarkon.core.uidata.ObserveAsEvents
import abkabk.azbarkon.domain.model.PoemAudioTrack
import abkabk.azbarkon.ui.components.SarvSlider
import abkabk.azbarkon.ui.components.Header
import abkabk.azbarkon.ui.components.HeaderAction
import abkabk.azbarkon.ui.theme.SarvTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.arrow_drop_down
import sarv.shared.generated.resources.cd_close_find_bar
import sarv.shared.generated.resources.cd_select_track
import sarv.shared.generated.resources.close
import sarv.shared.generated.resources.find_in_poem_hint
import sarv.shared.generated.resources.pause
import sarv.shared.generated.resources.play
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import abkabk.azbarkon.core.designsystem.LocalSarvDimensions
import abkabk.azbarkon.core.ui.LocalWindowSizeClass
import abkabk.azbarkon.core.ui.WindowWidthSizeClass
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight

private const val RING_SPIN_DURATION_MS = 3000
private const val MILLIS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60

@Composable
fun PoemDetailRoot(
    poemId: Int,
    onBackClick: () -> Unit,
    onNavigateToTasvirNegar: (poemId: Int, initialText: String?) -> Unit,
    onNavigateToMemorizationPractice: (Int) -> Unit,
    viewModel: PoemDetailViewModel = koinViewModel { parametersOf(poemId) },
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PoemDetailEvent.NavigateToTasvirNegar -> onNavigateToTasvirNegar(poemId, event.initialText)
            PoemDetailEvent.NavigateToMemorizationPractice -> onNavigateToMemorizationPractice(poemId)
        }
    }

    BaseScreen(
        screenState = state.screenState,
        onRetry = { viewModel.onAction(PoemDetailAction.OnRetryLoadTracks) },
    ) {
        PoemDetailScreen(
            state = state,
            audioState = audioState,
            onAction = viewModel::onAction,
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun PoemDetailScreen(
    state: PoemDetailState,
    audioState: AudioPlayerUiState,
    onAction: (PoemDetailAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val findFocusRequester = remember { FocusRequester() }
    val currentOnAction by rememberUpdatedState(onAction)
    val isExpanded = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

    LaunchedEffect(state.isFindBarVisible) {
        if (state.isFindBarVisible) {
            findFocusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(state.scrollToVerseId, state.verses) {
        val targetVerseId = state.scrollToVerseId ?: return@LaunchedEffect
        val targetIndex = state.verses.indexOfFirst { it.id == targetVerseId }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
            currentOnAction(PoemDetailAction.OnScrollConsumed)
        }
    }

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        topBar = {
            Column {
                Header(
                    title = state.poetName,
                    subtitle = state.subtitle,
                    onBackClick = onBackClick,
                    action =
                        HeaderAction.Bookmark(isBookmarked = state.isBookmarked) {
                            onAction(PoemDetailAction.OnBookmarkClick)
                        },
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            if (!isExpanded) {
                PoemDetailBottomBar(
                    state = state,
                    audioState = audioState,
                    findFocusRequester = findFocusRequester,
                    keyboardController = keyboardController,
                    focusManager = focusManager,
                    onAction = onAction,
                )
            } else {
                Column(
                    modifier = Modifier
                        .keyboardAboveIme()
                        .padding(end = LocalSarvDimensions.current.dimen96, start = LocalSarvDimensions.current.dimen16)
                        .padding(bottom = LocalSarvDimensions.current.dimen24),
                ) {
                    if (state.isFindBarVisible) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FindTextField(
                                modifier = Modifier.weight(1f).focusRequester(findFocusRequester),
                                value = state.findInput,
                                placeholder = stringResource(Res.string.find_in_poem_hint),
                                onValueChange = { query -> onAction(PoemDetailAction.OnFindQueryChange(query)) },
                                onSearch = { onAction(PoemDetailAction.OnFindSubmit) },
                            )

                            IconButton(onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onAction(PoemDetailAction.OnFindBarClose)
                            }) {
                                Icon(
                                    modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                                    painter = painterResource(Res.drawable.close),
                                    contentDescription = stringResource(Res.string.cd_close_find_bar),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    TrackPlayerCard(
                        tracks = audioState.tracks,
                        activeTrackUrl = audioState.activeTrackUrl,
                        onPlayPauseClick = { onAction(PoemDetailAction.OnTrackPlayPauseClick(it)) },
                        onSelectTrack = { onAction(PoemDetailAction.OnTrackSelect(it)) },
                        onSeekChange = { track, p -> onAction(PoemDetailAction.OnTrackSeekChanged(track, p)) },
                        onSeekFinish = { track, p -> onAction(PoemDetailAction.OnTrackSeekFinished(track, p)) },
                    )
                }
            }
        },
        snackbarHost = {
            SarvSnackbarHost(hostState = LocalSnackbarHostState.current)
        },
    ) { paddingValues ->

        val realClipboard = LocalClipboard.current
        val clipboardManager = koinInject<abkabk.azbarkon.core.platform.ClipboardManager>()
        val capturingClipboard =
            remember(realClipboard) {
                object : Clipboard by realClipboard {
                    override suspend fun setClipEntry(clipEntry: ClipEntry?) {
                        realClipboard.setClipEntry(clipEntry)
                        clipboardManager.readClipboardText()?.let { text ->
                            currentOnAction(PoemDetailAction.OnTextCopied(text))
                        }
                    }
                }
            }

        CompositionLocalProvider(LocalClipboard provides capturingClipboard) {
            SelectionContainer {
                if (isExpanded) {
                    PoemDetailExpandedLayout(listState, state, onAction, paddingValues)
                } else {
                    PoemDetailCompactLayout(listState, state, paddingValues)
                }
            }
        }
    }
}

@Composable
private fun PoemDetailExpandedLayout(
    listState: LazyListState,
    state: PoemDetailState,
    onAction: (PoemDetailAction) -> Unit,
    paddingValues: PaddingValues,
) {
    Row(
        modifier = Modifier.fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                end = LocalSarvDimensions.current.dimen16,
            ),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
                .padding(horizontal = LocalSarvDimensions.current.dimen16),
            contentPadding = PaddingValues(vertical = LocalSarvDimensions.current.dimen16),
        ) {
            items(
                items = state.verses,
                key = { verse -> verse.id },
            ) { verse ->
                PoemVerseItem(
                    verse = verse,
                    highlightQuery = state.highlightQuery,
                )
            }
            item {
                PoemOrnamentalDivider(
                    modifier = Modifier.padding(top = LocalSarvDimensions.current.dimen24),
                )
            }
        }
        PoemActionBar(
            isLiked = state.isLiked,
            isProse = state.isProse,
            onSearchClick = { onAction(PoemDetailAction.OnSearchClick) },
            onShareClick = { onAction(PoemDetailAction.OnShareClick) },
            onLikeClick = { onAction(PoemDetailAction.OnLikeClick) },
            onImageCreatorClick = { onAction(PoemDetailAction.OnImageCreatorClick) },
            onMemorizeClick = { onAction(PoemDetailAction.OnMemorizeClick) },
            isExpanded = true,
            modifier = Modifier.padding(
                start = LocalSarvDimensions.current.dimen16,
                top = LocalSarvDimensions.current.dimen16,
            ),
        )
    }
}

@Composable
private fun PoemDetailCompactLayout(
    listState: LazyListState,
    state: PoemDetailState,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
        contentPadding = PaddingValues(LocalSarvDimensions.current.dimen16),
    ) {
        items(
            items = state.verses,
            key = { verse -> verse.id },
        ) { verse ->
            PoemVerseItem(
                verse = verse,
                highlightQuery = state.highlightQuery,
            )
        }
        item {
            PoemOrnamentalDivider(
                modifier = Modifier.padding(top = LocalSarvDimensions.current.dimen24),
            )
        }
    }
}

@Composable
private fun PoemDetailBottomBar(
    state: PoemDetailState,
    audioState: AudioPlayerUiState,
    findFocusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    onAction: (PoemDetailAction) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .keyboardAboveIme()
                .fillMaxWidth()
                .padding(horizontal = LocalSarvDimensions.current.dimen16)
                .padding(bottom = LocalSarvDimensions.current.dimen24),
        verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen10),
    ) {
        if (state.isFindBarVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FindTextField(
                    modifier = Modifier.weight(1f).focusRequester(findFocusRequester),
                    value = state.findInput,
                    placeholder = stringResource(Res.string.find_in_poem_hint),
                    onValueChange = { query -> onAction(PoemDetailAction.OnFindQueryChange(query)) },
                    onSearch = {
                        onAction(PoemDetailAction.OnFindSubmit)
                    },
                )

                IconButton(onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onAction(PoemDetailAction.OnFindBarClose)
                }) {
                    Icon(
                        modifier = Modifier.size(LocalSarvDimensions.current.dimen24),
                        painter = painterResource(Res.drawable.close),
                        contentDescription = stringResource(Res.string.cd_close_find_bar),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            TrackPlayerCard(
                tracks = audioState.tracks,
                activeTrackUrl = audioState.activeTrackUrl,
                onPlayPauseClick = { onAction(PoemDetailAction.OnTrackPlayPauseClick(it)) },
                onSelectTrack = { onAction(PoemDetailAction.OnTrackSelect(it)) },
                onSeekChange = { track, p -> onAction(PoemDetailAction.OnTrackSeekChanged(track, p)) },
                onSeekFinish = { track, p -> onAction(PoemDetailAction.OnTrackSeekFinished(track, p)) },
            )

            PoemActionBar(
                isLiked = state.isLiked,
                isProse = state.isProse,
                onSearchClick = { onAction(PoemDetailAction.OnSearchClick) },
                onShareClick = { onAction(PoemDetailAction.OnShareClick) },
                onLikeClick = { onAction(PoemDetailAction.OnLikeClick) },
                onImageCreatorClick = { onAction(PoemDetailAction.OnImageCreatorClick) },
                onMemorizeClick = { onAction(PoemDetailAction.OnMemorizeClick) },
            )
        }
    }
}

@Composable
private fun TrackSelector(
    tracks: List<TrackPlaybackUiState>,
    selected: TrackPlaybackUiState,
    onSelectTrack: (PoemAudioTrack) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { menuExpanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8),
        ) {
            Text(
                text = trackLabel(selected.track),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = LocalSarvDimensions.current.dimen8),
            )
            Icon(
                painter = painterResource(Res.drawable.arrow_drop_down),
                contentDescription = stringResource(Res.string.cd_select_track),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen22),
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            tracks.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = trackLabel(item.track),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onSelectTrack(item.track)
                    },
                )
            }
        }
    }
}

private fun trackLabel(track: PoemAudioTrack): String {
    val artist = track.artist?.takeIf { it.isNotBlank() }
    val title = track.title?.takeIf { it.isNotBlank() }
    return when {
        artist != null && title != null -> "$artist • $title"
        artist != null -> artist
        title != null -> title
        else -> ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackPlayerCard(
    tracks: List<TrackPlaybackUiState>,
    activeTrackUrl: String?,
    onPlayPauseClick: (PoemAudioTrack) -> Unit,
    onSelectTrack: (PoemAudioTrack) -> Unit,
    onSeekChange: (PoemAudioTrack, Float) -> Unit,
    onSeekFinish: (PoemAudioTrack, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTrack = tracks.firstOrNull { it.track.url == activeTrackUrl } ?: return
    var dragProgress by remember { mutableStateOf<Float?>(null) }
    val displayedProgress = dragProgress ?: activeTrack.progress

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LocalSarvDimensions.current.dimen16))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = LocalSarvDimensions.current.dimen1,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(LocalSarvDimensions.current.dimen16),
            ).padding(LocalSarvDimensions.current.dimen8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen2)
    ) {
        PlayPauseButton(
            isPlaying = activeTrack.isPlaying,
            isLoading = activeTrack.isLoading,
            onClick = { onPlayPauseClick(activeTrack.track) },
        )


        Column(modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen4)) {

            TrackSelector(
                tracks = tracks,
                selected = activeTrack,
                onSelectTrack = onSelectTrack,
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalSarvDimensions.current.dimen8)
            ) {

                SarvSlider(
                    value = displayedProgress,
                    onValueChange = {
                        dragProgress = it
                        onSeekChange(activeTrack.track, it)
                    },
                    onValueChangeFinished = {
                        val finalValue = dragProgress ?: activeTrack.progress
                        onSeekFinish(activeTrack.track, finalValue)
                        dragProgress = null
                    },
                    modifier = Modifier.weight(1f).height(LocalSarvDimensions.current.dimen16),
                )

                Text(
                    modifier = modifier.padding(end = LocalSarvDimensions.current.dimen6),
                    text = "${formatMs(activeTrack.positionMs)} / ${formatMs(activeTrack.durationMs)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }


    }


}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {

    val iconTint = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .size(LocalSarvDimensions.current.dimen48)
            .clip(CircleShape)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(LocalSarvDimensions.current.dimen40)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (isPlaying) Res.drawable.pause else Res.drawable.play),
                contentDescription = if (isPlaying) "توقف" else "پخش",
                tint = iconTint,
                modifier = Modifier.size(LocalSarvDimensions.current.dimen20),
            )
        }

        if (isLoading) {

            val ringProgress = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                ringProgress.snapTo(0f)
                ringProgress.animateTo(1f, animationSpec = tween(RING_SPIN_DURATION_MS))
            }
            CircularProgressIndicator(
                progress = { ringProgress.value },
                modifier = Modifier.size(LocalSarvDimensions.current.dimen48),
                strokeWidth = LocalSarvDimensions.current.dimen2,
                color = iconTint,
                trackColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun PoemDetailScreenPreview() {
    SarvTheme {
        PoemDetailScreen(
            state =
                PoemDetailState(
                    poetName = "حافظ",
                    subtitle = "غزل شماره ۷",
                    verses =
                        listOf(
                            PoemVerseUi(
                                id = "0--1",
                                text = "شرح بیت",
                                positionType = PoemVersePositionType.Comment,
                            ),
                            PoemVerseUi(
                                id = "1-0",
                                text = "الا یا ایها الساقی ادر کاسا و ناولها",
                                positionType = PoemVersePositionType.Right,
                            ),
                            PoemVerseUi(
                                id = "1-1",
                                text = "که شد پیراهنت خونچکان من الایام",
                                positionType = PoemVersePositionType.Left,
                            ),
                            PoemVerseUi(
                                id = "2-4",
                                text = "بیت مستقل",
                                positionType = PoemVersePositionType.Single,
                            ),
                        ),
                    isLiked = true,
                ),
            audioState = AudioPlayerUiState(),
            onAction = {},
            onBackClick = {},
        )
    }
}

