package com.fathan.e_commerce.features.chat.ui.components

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.io.IOException

@Composable
fun AudioMessageContent(
    audioData: String, // Format: "url|duration"
    isCurrentUser: Boolean
) {
// ✅ Parse JSON format
    val (audioUrl, totalDuration) = remember(audioData) {
        try {
            val json = Json.parseToJsonElement(audioData).jsonObject
            val url = json["url"]?.jsonPrimitive?.content ?: ""
            val duration = json["duration"]?.jsonPrimitive?.longOrNull ?: 0L
            url to duration
        } catch (e: Exception) {
            // ✅ Fallback to old pipe format for backward compatibility
            Log.w("AudioMessage", "Failed to parse JSON, trying pipe format: ${e.message}")
            val parts = audioData.split("|")
            val url = parts.getOrNull(0) ?: ""
            val duration = parts.getOrNull(1)?.toLongOrNull() ?: 0L
            url to duration
        }
    }

    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setOnCompletionListener {
                isPlaying = false
                currentPosition = 0L
            }
            setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "Error: what=$what, extra=$extra")
                hasError = true
                isPlaying = false
                true
            }
        }
    }

    // Update current position while playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && mediaPlayer.isPlaying) {
                currentPosition = mediaPlayer.currentPosition.toLong()
                delay(100)
            }
        }
    }

    // Cleanup
    DisposableEffect(audioUrl) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error releasing player", e)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ✅ Play/Pause Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                .clickable {
                    if (hasError) return@clickable

                    try {
                        if (isPlaying) {
                            // Pause
                            mediaPlayer.pause()
                            isPlaying = false
                        } else {
                            if (mediaPlayer.currentPosition == 0) {
                                // Start from beginning
                                isLoading = true
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(audioUrl)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    isLoading = false
                                    mediaPlayer.start()
                                    isPlaying = true
                                }
                            } else {
                                // Resume
                                mediaPlayer.start()
                                isPlaying = true
                            }
                        }
                    } catch (e: IOException) {
                        Log.e("AudioPlayer", "Error playing audio", e)
                        hasError = true
                        isLoading = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(24.dp),
                    tint = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ✅ Waveform Visualization
        AudioWaveform(
            isPlaying = isPlaying,
            progress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
            isCurrentUser = isCurrentUser,
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        )

        // ✅ Duration / Current Time
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDuration(if (isPlaying) currentPosition else totalDuration),
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasError) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AudioWaveform(
    isPlaying: Boolean,
    progress: Float,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    // Generate random heights for waveform
    val barHeights = remember {
        List(25) { 0.3f + Math.random().toFloat() * 0.7f }
    }

    // Animate playing bars
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier) {
        val barCount = barHeights.size
        val barWidth = size.width / (barCount * 1.5f)
        val spacing = barWidth * 0.5f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val height = maxHeight * barHeights[i]
            val x = i * (barWidth + spacing)

            // Determine color based on progress and play state
            val isPast = (i.toFloat() / barCount) <= progress
            val color = when {
                isCurrentUser && isPast -> Color.White
                isCurrentUser -> Color.White.copy(alpha = 0.3f)
                isPast -> Color(0xFF075E54)
                else -> Color.Gray.copy(alpha = 0.3f)
            }

            // Animate height if playing
            val animatedHeight = if (isPlaying && isPast) {
                height * (0.8f + 0.2f * kotlin.math.sin((animatedProgress + i * 0.1f) * Math.PI.toFloat() * 2))
            } else {
                height
            }

            drawRect(
                color = color,
                topLeft = Offset(x, (maxHeight - animatedHeight) / 2),
                size = Size(barWidth, animatedHeight)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}