package abkabk.azbarkon.features.poems.details

import abkabk.azbarkon.core.domain.result.onFailure
import abkabk.azbarkon.core.domain.result.onSuccess
import abkabk.azbarkon.core.player.AudioPlaybackState
import abkabk.azbarkon.core.player.AudioPlayer
import abkabk.azbarkon.core.player.AudioPlayerListener
import abkabk.azbarkon.core.uidata.BaseViewModel
import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.core.uidata.UiText
import abkabk.azbarkon.core.uidata.toUiText
import abkabk.azbarkon.domain.model.PoemAudioTrack
import abkabk.azbarkon.domain.model.memorization.MemorizationError
import abkabk.azbarkon.domain.platform.ShareService
import abkabk.azbarkon.domain.repository.MemorizationRepository
import abkabk.azbarkon.domain.repository.PoemRepository
import abkabk.azbarkon.domain.repository.SavedPoemRepository
import abkabk.azbarkon.domain.usecase.BuildShareTextUseCase
import abkabk.azbarkon.domain.usecase.StartMemorizationFromPoemUseCase
import androidx.lifecycle.viewModelScope
import sarv.shared.generated.resources.Res
import sarv.shared.generated.resources.error_db_query
import sarv.shared.generated.resources.memorization_max_active_error
import sarv.shared.generated.resources.memorization_poem_not_error
import sarv.shared.generated.resources.memorization_card_not_error
import sarv.shared.generated.resources.search_empty_query
import sarv.shared.generated.resources.search_not_found_in_poem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val MIN_RING_VISIBLE_MS = 800.milliseconds

class PoemDetailViewModel(
    private val poemRepository: PoemRepository,
    private val savedPoemRepository: SavedPoemRepository,
    private val memorizationRepository: MemorizationRepository,
    private val shareService: ShareService,
    private val buildShareText: BuildShareTextUseCase,
    private val startMemorizationFromPoem: StartMemorizationFromPoemUseCase,
    private val poemId: Int,
    private val player: AudioPlayer,
) : BaseViewModel<PoemDetailAction, PoemDetailState, PoemDetailEvent>(
    initialState = PoemDetailState(),
) {

    private val _audioState = MutableStateFlow(AudioPlayerUiState())
    val audioState: StateFlow<AudioPlayerUiState> = _audioState.asStateFlow()

    private var preparedTrackUrl: String? = null
    private var loadingJob: Job? = null

    init {
        Napier.d(message = "PoemDetail opened for poemId=$poemId", tag = "PoemDebug")
    }

    private val playerListener = object : AudioPlayerListener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val activeUrl = _audioState.value.activeTrackUrl ?: return
            updateTrack(activeUrl) { it.copy(isPlaying = isPlaying) }
        }

        override fun onStateChanged(state: AudioPlaybackState) {
            val activeUrl = _audioState.value.activeTrackUrl ?: return
            when (state) {
                AudioPlaybackState.BUFFERING -> startLoading(activeUrl)
                AudioPlaybackState.READY -> {
                    if (loadingJob == null) {
                        updateTrack(activeUrl) { it.copy(isLoading = false) }
                    }
                }
                AudioPlaybackState.ENDED -> updateTrack(activeUrl) {
                    it.copy(isPlaying = false, progress = 1f)
                }
                AudioPlaybackState.IDLE -> Unit
                AudioPlaybackState.ERROR -> {
                    updateTrack(activeUrl) { it.copy(isLoading = false, isPlaying = false) }
                    viewModelScope.launch {
                        setState {
                            copy(screenState = UiScreenState.Error(message = UiText.DynamicString("پخش صدا با خطا مواجه شد")))
                        }
                    }
                }
            }
        }

        override fun onError(message: String) {
            val activeUrl = _audioState.value.activeTrackUrl ?: return
            updateTrack(activeUrl) { it.copy(isLoading = false, isPlaying = false) }
            viewModelScope.launch {
                setState {
                    copy(screenState = UiScreenState.Error(message = UiText.DynamicString("پخش صدا با خطا مواجه شد")))
                }
            }
        }
    }

    init {
        player.addListener(playerListener)
        startProgressTicker()
        onAction(PoemDetailAction.OnLoad)
        loadTracks()
    }

    override fun onCleared() {
        player.removeListener(playerListener)
        if (player.isPlaying) player.pause()
        player.release()
        super.onCleared()
    }

    private fun startProgressTicker() {
        viewModelScope.launch {
            while (isActive) {
                val audio = _audioState.value
                val activeUrl = audio.activeTrackUrl
                if (activeUrl != null && player.isPlaying && player.duration > 0) {
                    val activeState = audio.tracks.firstOrNull { it.track.url == activeUrl }
                    if (activeState != null && !activeState.isSeeking) {
                        updateTrack(activeUrl) {
                            it.copy(
                                progress = player.currentPosition.toFloat() / player.duration,
                                positionMs = player.currentPosition,
                                durationMs = player.duration,
                            )
                        }
                    }
                }
                delay(500.milliseconds)
            }
        }
    }

    override fun onAction(action: PoemDetailAction) {
        when (action) {
            PoemDetailAction.OnLoad -> loadPoemDetail()

            PoemDetailAction.OnRetryLoadTracks -> loadTracks()

            is PoemDetailAction.OnTrackPlayPauseClick -> togglePlayPause(action.track)
            is PoemDetailAction.OnTrackSelect -> onTrackSelect(action.track)
            is PoemDetailAction.OnTrackSeekChanged -> onSeekChanged(action.track, action.progress)
            is PoemDetailAction.OnTrackSeekFinished -> onSeekFinished(action.track, action.progress)

            PoemDetailAction.OnSearchClick -> toggleFindBar()

            is PoemDetailAction.OnFindQueryChange -> {
                setState { copy(findInput = action.query) }
            }

            PoemDetailAction.OnFindSubmit -> applyFindQuery(state.value.findInput)

            PoemDetailAction.OnFindBarClose -> closeFindBar()

            PoemDetailAction.OnScrollConsumed -> {
                setState { copy(scrollToVerseId = null) }
            }

            PoemDetailAction.OnShareClick -> sharePoem()

            is PoemDetailAction.OnTextCopied -> setState { copy(copiedText = action.text) }

            PoemDetailAction.OnLikeClick -> toggleLike()

            PoemDetailAction.OnBookmarkClick -> toggleBookmark()

            PoemDetailAction.OnImageCreatorClick -> navigateToTasvirNegar()

            PoemDetailAction.OnMemorizeClick -> startMemorization()
        }
    }

    private fun loadTracks() {
        viewModelScope.launch {
            poemRepository.getPoemRecitations(poemId)
                .onSuccess { tracks ->
                    val trackStates = tracks.map { track -> TrackPlaybackUiState(track = track) }
                    _audioState.update {
                        it.copy(
                            tracks = trackStates,
                            activeTrackUrl = trackStates.firstOrNull()?.track?.url,
                        )
                    }
                }
                .onFailure { error ->
                    setState {
                        copy(screenState = UiScreenState.Error(message = error.toUiText(), retryable = true))
                    }
                }
        }
    }

    private fun onTrackSelect(track: PoemAudioTrack) {
        if (_audioState.value.activeTrackUrl == track.url) return
        switchToTrack(track)
    }

    private fun togglePlayPause(track: PoemAudioTrack) {
        val audio = _audioState.value
        val isThisTrackActive = audio.activeTrackUrl == track.url

        when {
            isThisTrackActive && player.isPlaying -> {
                player.pause()
                updateTrack(track.url) { it.copy(isPlaying = false) }
            }

            isThisTrackActive && !player.isPlaying -> {
                if (preparedTrackUrl == track.url) {
                    if (player.playbackState == AudioPlaybackState.ENDED) {
                        player.seekTo(0)
                        updateTrack(track.url) { it.copy(progress = 0f, positionMs = 0L) }
                    }
                    player.play()
                    updateTrack(track.url) { it.copy(isPlaying = true) }
                } else {
                    switchToTrack(track)
                }
            }

            else -> switchToTrack(track)
        }
    }

    private fun switchToTrack(track: PoemAudioTrack) {
        val audio = _audioState.value
        val previousUrl = audio.activeTrackUrl

        if (previousUrl != null) {
            val livePositionMs = player.currentPosition
            val liveDurationMs = player.duration
            player.pause()
            updateTrack(previousUrl) {
                it.copy(
                    isPlaying = false,
                    positionMs = livePositionMs,
                    durationMs = if (liveDurationMs > 0) liveDurationMs else it.durationMs,
                    progress = if (liveDurationMs > 0) {
                        livePositionMs.toFloat() / liveDurationMs
                    } else {
                        it.progress
                    },
                )
            }
        }

        val targetState = audio.tracks.firstOrNull { it.track.url == track.url } ?: return

        // Set active track BEFORE calling into the player, so listener
        // callbacks (onStateChanged/onIsPlayingChanged) route to the
        // correct track instead of the one we just paused.
        _audioState.update { it.copy(activeTrackUrl = track.url) }
        startLoading(track.url)

        track.url?.let { url ->
            player.setMediaUrl(url)
            preparedTrackUrl = url
        }

        if (targetState.positionMs > 0) {
            player.seekTo(targetState.positionMs)
        }
        player.play()

        updateTrack(track.url) {
            it.copy(isPlaying = true)
        }
    }

    private fun startLoading(trackUrl: String?) {
        if (trackUrl == null) return
        loadingJob?.cancel()
        updateTrack(trackUrl) { it.copy(isLoading = true) }
        loadingJob = viewModelScope.launch {
            delay(MIN_RING_VISIBLE_MS)
            loadingJob = null
            if (player.playbackState == AudioPlaybackState.BUFFERING) {
                // Still buffering: keep the ring; the READY event clears it.
                return@launch
            }
            updateTrack(trackUrl) { it.copy(isLoading = false) }
        }
    }
    private fun onSeekChanged(track: PoemAudioTrack, progress: Float) {
        updateTrack(track.url) { state ->
            val durationMs = state.durationMs
            state.copy(
                isSeeking = true,
                progress = progress,
                positionMs = (progress * durationMs).toLong(),
            )
        }
    }

    private fun onSeekFinished(track: PoemAudioTrack, progress: Float) {
        val audio = _audioState.value
        val state = audio.tracks.firstOrNull { it.track.url == track.url } ?: return
        val targetMs = (progress * state.durationMs).toLong()

        if (audio.activeTrackUrl == track.url) {
            player.seekTo(targetMs)
        }

        updateTrack(track.url) { it.copy(isSeeking = false, progress = progress, positionMs = targetMs) }
    }

    private fun updateTrack(url: String?, transform: (TrackPlaybackUiState) -> TrackPlaybackUiState) {
        _audioState.update { audio ->
            audio.copy(
                tracks = audio.tracks.map { if (it.track.url == url) transform(it) else it },
            )
        }
    }

    private fun loadPoemDetail() {
        viewModelScope.launch {
            setState { copy(screenState = UiScreenState.Loading) }

            when (val result = poemRepository.getPoemDetail(poemId)) {
                is abkabk.azbarkon.core.domain.result.Result.Success -> {
                    val detail = result.data
                    val isMemorizing = memorizationRepository.isPoemActive(poemId)
                    val verseGroups = detail.verses.groupBy { it.vorder }
                    val distichCount = verseGroups.count { (_, v) -> v.any { it.position == 1 } }
                    val isProse = verseGroups.size > 0 && distichCount * 2 < verseGroups.size
                    setState {
                        copy(
                            screenState = UiScreenState.Success,
                            poetName = detail.poetName,
                            subtitle = detail.title,
                            verses = detail.verses.map { it.toPoemVerseUi() },
                            isLiked = savedPoemRepository.isLiked(poemId),
                            isBookmarked = savedPoemRepository.isBookmarked(poemId),
                            isMemorizing = isMemorizing,
                            isProse = isProse,
                        )
                    }
                }
                is abkabk.azbarkon.core.domain.result.Result.Error -> {
                    val message = result.error.toUiText()
                    setState {
                        copy(screenState = UiScreenState.Error(message = message))
                    }
                }
            }
        }
    }

    private fun startMemorization() {
        viewModelScope.launch {
            when (val result = startMemorizationFromPoem(poemId)) {
                is StartMemorizationFromPoemUseCase.StartResult.AlreadyActive -> {
                    sendEvent(PoemDetailEvent.NavigateToMemorizationPractice)
                }
                is StartMemorizationFromPoemUseCase.StartResult.Success -> {
                    setState { copy(isMemorizing = true) }
                    sendEvent(PoemDetailEvent.NavigateToMemorizationPractice)
                }
                is StartMemorizationFromPoemUseCase.StartResult.Error -> {
                    val message = when (result.error) {
                        MemorizationError.MaxActivePoemsReached ->
                            UiText.Resource(Res.string.memorization_max_active_error)
                        else -> result.error.toUiText()
                    }
                    setState {
                        copy(screenState = UiScreenState.Error(message = message))
                    }
                }
            }
        }
    }

    private fun toggleFindBar() {
        val currentState = state.value
        if (currentState.isFindBarVisible) {
            closeFindBar()
            return
        }

        val prefilledInput =
            currentState.findInput.ifBlank {
                currentState.highlightQuery
            }

        setState {
            copy(
                isFindBarVisible = true,
                findInput = prefilledInput,
            )
        }
    }

    private fun closeFindBar() {
        setState {
            copy(
                isFindBarVisible = false,
                findInput = "",
                highlightQuery = "",
                scrollToVerseId = null,
            )
        }
    }

    private fun applyFindQuery(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            viewModelScope.launch {
                setState {
                    copy(screenState = UiScreenState.Error(message = UiText.Resource(Res.string.search_empty_query)))
                }
            }
            return
        }

        val matchingVerse = findFirstMatchingVerse(state.value.verses, trimmedQuery)
        if (matchingVerse == null) {
            viewModelScope.launch {
                setState {
                    copy(screenState = UiScreenState.Error(message = UiText.Resource(Res.string.search_not_found_in_poem)))
                }
            }
            return
        }

        setState {
            copy(
                highlightQuery = trimmedQuery,
                findInput = trimmedQuery,
                scrollToVerseId = matchingVerse.id,
            )
        }
    }

    private fun sharePoem() {
        val text = buildShareText(
            poetName = state.value.poetName,
            subtitle = state.value.subtitle,
            verseTexts = state.value.verses.map { it.text },
            selectedText = state.value.copiedText,
        )
        if (text.isBlank()) return

        shareService.shareText(
            text = text,
            title = state.value.subtitle.ifBlank { state.value.poetName },
        )
    }

    private fun toggleLike() {
        val isLiked = savedPoemRepository.toggleLike(poemId)
        setState { copy(isLiked = isLiked) }
    }

    private fun toggleBookmark() {
        val isBookmarked = savedPoemRepository.toggleBookmark(poemId)
        setState { copy(isBookmarked = isBookmarked) }
    }

    private fun navigateToTasvirNegar() {
        val initialText = state.value.copiedText
        viewModelScope.launch {
            sendEvent(PoemDetailEvent.NavigateToTasvirNegar(initialText))
        }
    }
}

private fun MemorizationError.toUiText(): UiText =
    when (this) {
        MemorizationError.MaxActivePoemsReached ->
            UiText.Resource(Res.string.memorization_max_active_error)
        MemorizationError.PoemNotFound ->
            UiText.Resource(Res.string.memorization_poem_not_error)
        MemorizationError.CardNotFound ->
            UiText.Resource(Res.string.memorization_card_not_error)
        MemorizationError.Unknown ->
            UiText.Resource(Res.string.error_db_query)
    }
