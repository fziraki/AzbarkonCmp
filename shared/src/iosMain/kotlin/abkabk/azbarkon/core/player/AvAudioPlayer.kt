@file:OptIn(ExperimentalForeignApi::class)

package abkabk.azbarkon.core.player

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

class AvAudioPlayer : AudioPlayer {

    private var player: AVPlayer? = null
    private val listeners = mutableSetOf<AudioPlayerListener>()
    private var endObserver: NSObjectProtocol? = null

    override val isPlaying: Boolean
        get() = (player?.rate ?: 0f) != 0f && player?.error == null

    @OptIn(ExperimentalForeignApi::class)
    override val currentPosition: Long
        get() {
            val time = player?.currentTime() ?: return 0L
            return CMTimeGetSeconds(time).toLong() * 1000
        }

    @OptIn(ExperimentalForeignApi::class)
    override val duration: Long
        get() {
            val time = player?.currentItem?.duration ?: return 0L
            val seconds = CMTimeGetSeconds(time)
            return if (seconds.isNaN()) 0L else (seconds * 1000).toLong()
        }

    override var playbackState: AudioPlaybackState = AudioPlaybackState.IDLE
        private set

    override fun setMediaUrl(url: String) {
        release()

        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)

        val nsUrl = NSURL.URLWithString(url) ?: return
        val item = AVPlayerItem(uRL = nsUrl)
        player = AVPlayer(playerItem = item)
        playbackState = AudioPlaybackState.BUFFERING
        notifyStateChanged()

        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = NSOperationQueue.mainQueue,
        ) {
            playbackState = AudioPlaybackState.ENDED
            notifyStateChanged()
            deactivateSession()
        }

        // NOTE: True readiness detection needs KVO on AVPlayerItem.status,
        // which isn't straightforward through cinterop. We currently mark
        // READY on play() instead of waiting for status == .readyToPlay.
    }

    override fun play() {
        AVAudioSession.sharedInstance().setActive(true, error = null)
        player?.play()
        playbackState = AudioPlaybackState.READY
        notifyIsPlayingChanged(true)
        notifyStateChanged()
    }

    override fun pause() {
        player?.pause()
        deactivateSession()
        notifyIsPlayingChanged(false)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun seekTo(positionMs: Long) {
        val cmTime = CMTimeMakeWithSeconds(positionMs / 1000.0, 1)
        player?.seekToTime(cmTime)
    }

    override fun addListener(listener: AudioPlayerListener) {
        listeners += listener
    }

    override fun removeListener(listener: AudioPlayerListener) {
        listeners -= listener
    }

    override fun release() {
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endObserver = null
        player?.pause()
        player = null
        playbackState = AudioPlaybackState.IDLE
        deactivateSession()
    }

    private fun deactivateSession() {
        // ponytail: no notifyOthersOnDeactivation - other apps stay paused until the user replays them
        AVAudioSession.sharedInstance().setActive(false, error = null)
    }

    private fun notifyStateChanged() {
        listeners.forEach { it.onStateChanged(playbackState) }
    }

    private fun notifyIsPlayingChanged(playing: Boolean) {
        listeners.forEach { it.onIsPlayingChanged(playing) }
    }
}
