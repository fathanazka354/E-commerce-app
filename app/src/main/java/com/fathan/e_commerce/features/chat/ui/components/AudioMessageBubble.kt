package com.fathan.e_commerce.features.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.features.components.AudioPlaybackState

@Composable
fun AudioMessageBubble(
    audioUrl: String?,
    messageId: String,
    isMe: Boolean,
    isUploading: Boolean,
    uploadFailed: Boolean,
    playbackState: AudioPlaybackState,
    onPlayPause: () -> Unit,
    onSpeedChange: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val isThisPlaying = playbackState.currentPlayingId == messageId && playbackState.isPlaying
    val isThisAudio = playbackState.currentPlayingId == messageId

    val currentPosition = if (isThisAudio) playbackState.currentPosition else 0
    val duration = if (isThisAudio) playbackState.duration else 0
    val speed = if (isThisAudio) playbackState.playbackSpeed else 1.0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Play/Pause button
        IconButton(
            onClick = onPlayPause,
            enabled = !isUploading && !uploadFailed && audioUrl != null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isMe) Color(0xFF128C7E) else Color(0xFF075E54)
                )
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Waveform or progress
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Progress slider
            if (isThisAudio && duration > 0) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { onSeek(it) },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isMe) Color(0xFF128C7E) else Color(0xFF075E54),
                        activeTrackColor = if (isMe) Color(0xFF128C7E) else Color(0xFF075E54),
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            } else {
                // Simple waveform placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                )
            }

            // Time and speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isThisAudio) {
                        formatTime(currentPosition) + " / " + formatTime(duration)
                    } else if (uploadFailed) {
                        "Upload Failed"
                    } else if (isUploading) {
                        "Uploading..."
                    } else {
                        "Voice Message"
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Speed control
                if (isThisAudio) {
                    TextButton(
                        onClick = onSpeedChange,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) Color(0xFF128C7E) else Color(0xFF075E54)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}