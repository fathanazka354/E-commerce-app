package com.fathan.e_commerce.features.chat.ui

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0

    fun startRecording(): File? {
        try {
            outputFile = File.createTempFile("audio_", ".m4a", context.cacheDir)

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)

                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            Log.d("AudioRecorder", "Recording started: ${outputFile?.absolutePath}")
            return outputFile

        } catch (e: IOException) {
            Log.e("AudioRecorder", "Recording failed", e)
            cleanup()
            return null
        }
    }

    fun stopRecording(): Pair<File?, Long>? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            val duration = System.currentTimeMillis() - startTime
            val file = outputFile

            Log.d("AudioRecorder", "Recording stopped. Duration: ${duration}ms")

            if (file?.exists() == true && file.length() > 0) {
                Pair(file, duration)
            } else {
                cleanup()
                null
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop recording failed", e)
            cleanup()
            null
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Cancel recording failed", e)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        recorder = null
        outputFile?.delete()
        outputFile = null
    }

    fun getDuration(): Long {
        return if (startTime > 0) {
            System.currentTimeMillis() - startTime
        } else 0
    }
}