package com.fathan.e_commerce.features.chat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import com.fathan.e_commerce.features.chat.ui.components.AudioMessageBubble
import com.fathan.e_commerce.features.chat.utils.TimeUtils
import com.fathan.e_commerce.features.components.AudioPlaybackState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatDetailScreen(
    roomId: String,
    myUserId: String,
    onBack: () -> Unit,
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val audioPlaybackState by viewModel.audioPlaybackState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Audio recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    val audioRecorder = remember { AudioRecorder(context) }

    // Debug log
    LaunchedEffect(myUserId) {
        Log.d("ChatDetail", "My User ID: $myUserId")
    }

    LaunchedEffect(state.messages.size) {
        Log.d("ChatDetail", "Messages count in UI: ${state.messages.size}")
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Log.e("ChatDetail", "Audio permission denied")
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.sendImage(it) }
    }

    // Update recording duration
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(100)
                recordingDuration = audioRecorder.getDuration()
            }
        }
    }

    // Auto scroll ketika ada message baru
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    LaunchedEffect(roomId) {
        viewModel.openRoom(roomId, myUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Column {
                // Recording overlay
                if (isRecording) {
                    RecordingOverlay(
                        duration = recordingDuration,
                        onCancel = {
                            audioRecorder.cancelRecording()
                            isRecording = false
                            recordingDuration = 0L
                        }
                    )
                }

                MessageInput(
                    text = input,
                    onTextChange = { input = it },
                    isRecording = isRecording,
                    onPickImage = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onStartRecording = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            audioRecorder.startRecording()
                            isRecording = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopRecording = {
                        val result = audioRecorder.stopRecording()
                        isRecording = false

                        result?.let { (file, duration) ->
                            file?.let { viewModel.sendAudioFile(it, duration) }
                        }

                        recordingDuration = 0L
                    },
                    onSend = {
                        viewModel.sendText(input)
                        input = ""
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ Show shimmer loading when loading
            if (state.isLoading) {
                ChatLoadingShimmer()
            } else if (state.messages.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No messages yet",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Start the conversation",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isMe = msg.isMe,
                            audioPlaybackState = audioPlaybackState,
                            onPlayAudio = { audioUrl ->
                                viewModel.playAudio(audioUrl, msg.id)
                            },
                            onSpeedChange = {
                                viewModel.cyclePlaybackSpeed()
                            },
                            onSeek = { position ->
                                viewModel.seekAudio(position)
                            }
                        )
                    }
                }
            }

            // Error message overlay
            state.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { /* Dismiss */ }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

// ✅ Shimmer Loading Composable
@Composable
fun ChatLoadingShimmer() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) { index ->
            if (index % 3 == 0) {
                // Sender message (right aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    ShimmerMessageBubble(
                        brush = brush,
                        isMe = true
                    )
                }
            } else {
                // Receiver message (left aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    ShimmerMessageBubble(
                        brush = brush,
                        isMe = false
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerMessageBubble(
    brush: Brush,
    isMe: Boolean
) {
    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isMe) 0.7f else 0.8f)
                .height(60.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isMe) 12.dp else 4.dp,
                        topEnd = if (isMe) 4.dp else 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(brush)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Timestamp shimmer
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
                .align(if (isMe) Alignment.End else Alignment.Start)
        )
    }
}

@Composable
fun RecordingOverlay(
    duration: Long,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pulsing red dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )

                Text(
                    text = formatDuration(duration),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "← Slide to cancel",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    audioPlaybackState: AudioPlaybackState,
    onPlayAudio: (String) -> Unit,
    onSpeedChange: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val isUploading = message.metadata?.get("uploading") == "true"
    val uploadFailed = message.metadata?.get("upload_failed") == "true"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        uploadFailed -> Color(0xFFFFCDD2)
                        isUploading -> Color(0xFFF5F5F5)
                        isMe -> Color(0xFFDCF8C6)
                        else -> Color(0xFFFFFFFF)
                    },
                    shape = RoundedCornerShape(
                        topStart = if (isMe) 12.dp else 4.dp,
                        topEnd = if (isMe) 4.dp else 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .padding(10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                when (message.type) {
                    MessageType.TEXT -> {
                        Row {
                            Text(
                                text = message.message ?: "",
                                color = Color.Black,
                                modifier = Modifier.weight(2f)
                            )
                            if (message.isMe) {
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = "Read status",
                                    modifier = Modifier
                                        .weight(.5f)
                                        .align(Alignment.Bottom)
                                        .size(12.dp),
                                    tint = if (message.isRead) Color.Blue else Color.Gray
                                )
                            }
                        }
                    }

                    MessageType.IMAGE -> {
                        Row {
                            Box {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                if (isUploading) {
                                    Box(
                                        modifier = Modifier
                                            .size(180.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                if (uploadFailed) {
                                    Box(
                                        modifier = Modifier
                                            .size(180.dp)
                                            .background(
                                                Color.Red.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Upload Failed",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            if (message.isMe) {
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = "Read status",
                                    modifier = Modifier
                                        .weight(.5f)
                                        .align(Alignment.Bottom)
                                        .size(12.dp),
                                    tint = if (message.isRead) Color.Blue else Color.Gray
                                )
                            }
                        }
                    }

                    MessageType.AUDIO -> {
                        Row {
                            AudioMessageBubble(
                                audioUrl = message.mediaUrl,
                                messageId = message.id,
                                isMe = isMe,
                                isUploading = isUploading,
                                uploadFailed = uploadFailed,
                                playbackState = audioPlaybackState,
                                onPlayPause = {
                                    message.mediaUrl?.let { onPlayAudio(it) }
                                },
                                onSpeedChange = onSpeedChange,
                                onSeek = onSeek
                            )
                            if (message.isMe) {
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = "Read status",
                                    modifier = Modifier
                                        .weight(.5f)
                                        .align(Alignment.Bottom)
                                        .size(12.dp),
                                    tint = if (message.isRead) Color.Blue else Color.Gray
                                )
                            }
                        }
                    }

                    else -> {
                        Text(message.message ?: "")
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUploading) {
                        Text(
                            text = "Sending...",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    } else {
                        Text(
                            text = TimeUtils.getRelativeTime(message.createdAt),
                            fontSize = 10.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    isRecording: Boolean,
    onPickImage: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPickImage) {
            Icon(Icons.Default.Image, contentDescription = "Image")
        }

        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type message") },
            enabled = !isRecording
        )

        if (text.isBlank()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartRecording()
                                val released = tryAwaitRelease()
                                if (released) {
                                    onStopRecording()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Hold to record",
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            IconButton(onClick = onSend, enabled = text.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}