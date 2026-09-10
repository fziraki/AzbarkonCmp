package abkabk.azbarkon.features.poems.details

import abkabk.azbarkon.core.uidata.UiScreenState
import abkabk.azbarkon.domain.model.PoemAudioTrack

data class PoemDetailState(
    val screenState: UiScreenState = UiScreenState.Loading,
    val poetName: String = "",
    val subtitle: String = "",
    val verses: List<PoemVerseUi> = emptyList(),
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isMemorizing: Boolean = false,
    val isFindBarVisible: Boolean = false,
    val findInput: String = "",
    val highlightQuery: String = "",
    val scrollToVerseId: String? = null,
    val copiedText: String? = null,
    val isProse: Boolean = false,
)


data class TrackPlaybackUiState(
    val track: PoemAudioTrack,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isSeeking: Boolean = false,
)

data class AudioPlayerUiState(
    val tracks: List<TrackPlaybackUiState> = emptyList(),
    val activeTrackUrl: String? = null,
)

sealed interface PoemDetailAction {
    data object OnLoad : PoemDetailAction
    data class OnTrackPlayPauseClick(val track: PoemAudioTrack) : PoemDetailAction
    data class OnTrackSelect(val track: PoemAudioTrack) : PoemDetailAction
    data class OnTrackSeekChanged(val track: PoemAudioTrack, val progress: Float) : PoemDetailAction
    data class OnTrackSeekFinished(val track: PoemAudioTrack, val progress: Float) : PoemDetailAction

    data object OnSearchClick : PoemDetailAction
    data class OnFindQueryChange(val query: String) : PoemDetailAction
    data object OnFindSubmit : PoemDetailAction
    data object OnFindBarClose : PoemDetailAction
    data object OnScrollConsumed : PoemDetailAction

    data object OnShareClick : PoemDetailAction
    data class OnTextCopied(val text: String) : PoemDetailAction
    data object OnLikeClick : PoemDetailAction
    data object OnBookmarkClick : PoemDetailAction
    data object OnImageCreatorClick : PoemDetailAction
    data object OnMemorizeClick : PoemDetailAction
    data object OnRetryLoadTracks : PoemDetailAction
}
sealed interface PoemDetailEvent {
    data object NavigateToMemorizationPractice : PoemDetailEvent
    data class NavigateToTasvirNegar(val initialText: String? = null) : PoemDetailEvent
}
