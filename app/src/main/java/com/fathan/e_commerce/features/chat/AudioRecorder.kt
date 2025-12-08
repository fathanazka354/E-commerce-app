package com.fathan.e_commerce.features.chat

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import java.io.File

/**
 * Simple audio recorder wrapper to keep ChatDetailScreen clean.
 *
 * Usage:
 *  val recorder = AudioRecorder(context)
 *  recorder.startRecording()
 *  val (uri, durationMs) = recorder.stopRecording()
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0L

    fun startRecording(): Boolean {
        return try {
            stopAndReleaseInternal()

            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            outputFile = File(context.cacheDir, fileName)

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Stops recording and returns Uri + duration in millis.
     */
    fun stopRecording(): Pair<Uri, Long>? {
        return try {
            recorder?.apply {
                stop()
            }
            val duration = System.currentTimeMillis() - startTime
            val file = outputFile

            stopAndReleaseInternal()

            if (file != null && file.exists()) {
                Pair(Uri.fromFile(file), duration)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopAndReleaseInternal()
            null
        }
    }

    private fun stopAndReleaseInternal() {
        try {
            recorder?.release()
        } catch (_: Exception) {
        } finally {
            recorder = null
        }
    }
}

/**
 * Format duration millis -> m:ss (e.g. 0:05, 1:23)
 */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

