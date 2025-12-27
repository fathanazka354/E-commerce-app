package com.fathan.e_commerce.features.components

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val currentPlayingId: String? = null
)

class AudioPlayerManager() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun playAudio(audioUrl: String, messageId: String) {
        try {
            // Jika sedang play audio yang sama, pause
            if (_playbackState.value.currentPlayingId == messageId && _playbackState.value.isPlaying) {
                pause()
                return
            }

            // Jika play audio berbeda, stop yang lama
            if (_playbackState.value.currentPlayingId != messageId) {
                stop()
            }

            // Jika audio yang sama tapi di-pause, resume
            if (_playbackState.value.currentPlayingId == messageId && !_playbackState.value.isPlaying) {
                resume()
                return
            }

            // Start new audio
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener { mp ->
                    val duration = mp.duration
                    _playbackState.value = _playbackState.value.copy(
                        duration = duration,
                        currentPlayingId = messageId
                    )

                    // Set playback speed
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        mp.playbackParams = mp.playbackParams.setSpeed(_playbackState.value.playbackSpeed)
                    }

                    mp.start()
                    _playbackState.value = _playbackState.value.copy(isPlaying = true)
                    startProgressTracking()

                    Log.d("AudioPlayer", "Playing: $audioUrl, Duration: $duration ms")
                }

                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        currentPosition = 0
                    )
                    progressJob?.cancel()
                    Log.d("AudioPlayer", "Playback completed")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayer", "Error: what=$what, extra=$extra")
                    stop()
                    true
                }
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to play audio", e)
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        progressJob?.cancel()
        Log.d("AudioPlayer", "Paused")
    }

    fun resume() {
        mediaPlayer?.start()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        startProgressTracking()
        Log.d("AudioPlayer", "Resumed")
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Stop error", e)
        }

        mediaPlayer = null
        progressJob?.cancel()
        _playbackState.value = AudioPlaybackState()
        Log.d("AudioPlayer", "Stopped")
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _playbackState.value = _playbackState.value.copy(currentPosition = position)
        Log.d("AudioPlayer", "Seeked to: $position ms")
    }

    fun setPlaybackSpeed(speed: Float) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (mediaPlayer?.playbackParams?.setSpeed(speed) != null){
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed)!!
            }
            _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
            Log.d("AudioPlayer", "Speed set to: ${speed}x")
        }
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(1.0f, 1.5f, 2.0f)
        val currentSpeed = _playbackState.value.playbackSpeed
        val nextSpeed = speeds[(speeds.indexOf(currentSpeed) + 1) % speeds.size]
        setPlaybackSpeed(nextSpeed)
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                val position = mediaPlayer?.currentPosition ?: 0
                _playbackState.value = _playbackState.value.copy(currentPosition = position)
                delay(100)
            }
        }
    }

    fun release() {
        stop()
    }
}