package com.fathan.e_commerce.features.chat.ui.components

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * MessageItem composable that renders a ChatMessage in the message thread.
 * Use key = { it.id } when listing.
 */
@Composable
fun MessageItem(message: ChatMessage) {
    val isMe = message.isMe

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            UserAvatar(message.initial, message.avatarColor ?: Color(0xFFE1BEE7))
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName ?: "Unknown",
                    color = Color.DarkGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Surface(
                shape = if (isMe) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp) else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                color = if (isMe) Color(0xFFE8EAF6) else Color.White,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.type == MessageType.REPLY) {
                        ReplyContent(message)
                    }

                    when (message.type) {
                        MessageType.IMAGE -> ImageMessageContent(message)
                        MessageType.AUDIO -> AudioMessageContent(message, isMe)
                        else -> Text(text = message.content ?: message.message ?: "", color = Color.Black)
                    }
                }
            }

            MessageMetaInfo(message, isMe)
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(message.initial, message.avatarColor ?: Color(0xFFE1BEE7))
        }
    }
}

@Composable
fun ImageMessageContent(message: ChatMessage) {
    val uri = message.imageUri ?: message.mediaUrl?.let { Uri.parse(it) }
    if (uri != null) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Sent Image",
            modifier = Modifier
                .height(200.dp)
                .widthIn(max = 250.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .height(150.dp)
                .width(200.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Image", color = Color.White)
        }
    }
}

@Composable
fun AudioMessageContent(message: ChatMessage, isMe: Boolean) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }

    DisposableEffect(message.mediaUrl, message.audioUri) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null && mediaPlayer!!.isPlaying) {
            currentPositionMs = mediaPlayer!!.currentPosition.toLong()
            durationMs = mediaPlayer!!.duration.toLong()
            delay(200)
        }
    }

    fun togglePlay() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            return
        }

        if (mediaPlayer == null) {
            val uri = message.audioUri ?: message.mediaUrl?.let { Uri.parse(it) }
            if (uri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, uri)
                    prepare()
                    durationMs = duration.toLong()
                    setOnCompletionListener {
                        isPlaying = false
                        currentPositionMs = 0L
                    }
                }
            }
        }
        mediaPlayer?.start()
        isPlaying = true
    }

    val progress = remember(currentPositionMs, durationMs) {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val currentText = formatDuration(currentPositionMs)
    val totalText = if (durationMs > 0) formatDuration(durationMs) else (message.audioDuration ?: "0:00")

    Row(modifier = Modifier
        .width(200.dp)
        .clickable { togglePlay() }
        .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = if (isMe) Color(0xFF5C6BC0) else Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(4.dp), trackColor = Color.LightGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$currentText / $totalText", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ReplyContent(message: ChatMessage) {
    Row(modifier = Modifier.padding(bottom = 8.dp).height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Color(0xFF7986CB), RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = message.replyToName ?: "", color = Color(0xFF7986CB), fontWeight = FontWeight.Bold)
            Text(text = message.replyText ?: "", color = Color.Gray, maxLines = 2)
        }
    }
}

@Composable
fun MessageMetaInfo(message: ChatMessage, isMe: Boolean) {
    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isMe) {
            Text(text = message.time, color = Color.LightGray, modifier = Modifier.padding(end = 8.dp), fontSize = 12.sp)
        }

        if (!isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = message.time, color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun UserAvatar(initial: String, color: Color) {
    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// small helper to format millis -> m:ss
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
