package com.fathan.e_commerce.features.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import android.media.MediaPlayer
import androidx.compose.material3.LinearProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.Utils
import com.fathan.e_commerce.data.local.Message
import com.fathan.e_commerce.data.local.MessageType

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatDetailScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    // 🔥 ViewModel
    val chatViewModel: ChatViewModel = hiltViewModel()
    val uiState by chatViewModel.uiState.collectAsState()

    // Audio recorder (infra, tetap di UI)
    val audioRecorder = remember { AudioRecorder(context) }

    // --- State dari ViewModel ---
    val messages = uiState.messages
    val listState = rememberLazyListState()

    // Grouping by date
    val groupedMessages = remember(messages) {
        messages.groupBy { it.date }
    }

    // Auto-scroll saat jumlah message berubah
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size + groupedMessages.size)
        }
    }

    // 1. Image Picker
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                chatViewModel.sendImage(uri)
            }
        }
    )

    // 2. Audio Permission (tetap di UI)
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasAudioPermission = isGranted }
    )

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = { ChatDetailTopBar(onBack) },
        bottomBar = {
            ChatInputArea(
                onSendText = { text ->
                    chatViewModel.sendText(text)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                },
                onAttachImage = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onStartRecording = {
                    if (hasAudioPermission) {
                        val started = audioRecorder.startRecording()
                        if (!started) {
                            Toast.makeText(
                                context,
                                "Failed to start recording",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecordingAndSend = {
                    if (!hasAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@ChatInputArea
                    }

                    val result = audioRecorder.stopRecording()
                    if (result != null) {
                        val (uri, durationMs) = result
                        chatViewModel.sendAudio(uri, durationMs)

                        Toast.makeText(context, "Audio sent!", Toast.LENGTH_SHORT).show()
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    } else {
                        Toast.makeText(context, "No audio recorded", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
        ) {
            groupedMessages.forEach { (date, messagesForDate) ->
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Utils.getDayHeader(date),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(messagesForDate) { message ->
                    MessageItem(message)
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val isMe = message.isMe

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            UserAvatar(message.initial, message.avatarColor)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.DarkGray),
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            Surface(
                shape = if (isMe) {
                    RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                } else {
                    RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                },
                color = if (isMe) Color(0xFFE8EAF6) else Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.type == MessageType.REPLY) {
                        ReplyContent(message)
                    }

                    when (message.type) {
                        MessageType.IMAGE -> ImageMessageContent(message)
                        MessageType.AUDIO -> AudioMessageContent(message, isMe)
                        else -> Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                        )
                    }
                }
            }

            MessageMetaInfo(message, isMe)
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(message.initial, message.avatarColor)
        }
    }
}

@Composable
fun ImageMessageContent(message: Message) {
    if (message.imageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(message.imageUri),
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
fun AudioMessageContent(message: Message, isMe: Boolean) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // State untuk waktu dan durasi
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }

    // Bersihkan MediaPlayer saat audioUri atau composable berubah
    DisposableEffect(message.audioUri) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Effect untuk update progress secara berkala saat sedang play
    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && mediaPlayer != null && mediaPlayer!!.isPlaying) {
            currentPositionMs = mediaPlayer!!.currentPosition.toLong()
            durationMs = mediaPlayer!!.duration.toLong()
            kotlinx.coroutines.delay(200) // update tiap 200ms
        }
    }

    fun togglePlay() {
        if (isPlaying) {
            // Pause
            mediaPlayer?.pause()
            isPlaying = false
            return
        }

        // Init mediaPlayer kalau belum ada
        if (mediaPlayer == null && message.audioUri != null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, message.audioUri)
                prepare()
                durationMs = duration.toLong()
                setOnCompletionListener {
                    isPlaying = false
                    currentPositionMs = 0L
                }
            }
        }

        // Start dari posisi terakhir
        mediaPlayer?.start()
        isPlaying = true
    }

    // Hitung progress [0f..1f]
    val progress = remember(currentPositionMs, durationMs) {
        if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val currentText = formatDuration(currentPositionMs)
    // totalText pakai durasi real kalau ada; fallback ke message.audioDuration
    val totalText = if (durationMs > 0) {
        formatDuration(durationMs)
    } else {
        message.audioDuration ?: "0:00"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(200.dp)
            .clickable { togglePlay() }
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play Audio",
            tint = if (isMe) Color(0xFF5C6BC0) else Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (isMe) Color(0xFF5C6BC0) else Color.Gray,
                trackColor = Color.LightGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$currentText / $totalText",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun ReplyContent(message: Message) {
    Row(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Color(0xFF7986CB), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = message.replyToName ?: "",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF7986CB),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = message.replyText ?: "",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                maxLines = 2
            )
        }
    }
}

@Composable
fun MessageMetaInfo(message: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (isMe) {
            Text(
                text = message.time,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        if (message.reactions.isNotEmpty()) {
            message.reactions.forEach { reaction ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = reaction,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (!isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message.time,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
            )
        }
    }
}

@Composable
fun ChatInputArea(
    onSendText: (String) -> Unit,
    onAttachImage: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecordingAndSend: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val view = LocalView.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Input Box
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = if (isRecording) "Recording..." else text,
                    onValueChange = { if (!isRecording) text = it },
                    enabled = !isRecording,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = if (isRecording) Color.Red else Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty() && !isRecording) {
                            Text("Type here..", color = Color.LightGray, fontSize = 16.sp)
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (text.isNotBlank()) {
                            onSendText(text)
                            text = ""
                        }
                    })
                )

                if (!isRecording) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onAttachImage, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Image, "Image", tint = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { /* TODO Camera logic */ }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.CameraAlt, "Camera", tint = Color.LightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Dynamic Button (Send vs Mic)
        Surface(
            shape = CircleShape,
            color = if (isRecording) Color.Red else Color(0xFF5C6BC0),
            shadowElevation = 4.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (text.isNotBlank()) {
                                    onSendText(text)
                                    text = ""
                                } else {
                                    // No text -> hint user to long press
                                    Toast
                                        .makeText(
                                            context,
                                            "Hold to record voice message",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            },
                            onLongPress = {
                                isRecording = true
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onStartRecording()
                            },
                            onPress = {
                                tryAwaitRelease()
                                if (isRecording) {
                                    isRecording = false
                                    onStopRecordingAndSend()
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                }
                            }
                        )
                    }
            ) {
                Icon(
                    imageVector = if (text.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = "Action",
                    tint = Color.White
                )
            }
        }
    }
}

// --- Top Bar and Avatar ---
@Composable
fun ChatDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFE1BEE7)),
            contentAlignment = Alignment.Center
        ) {
            Text("UT", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Untitled Team", fontWeight = FontWeight.Bold)
            Text("John, Aliena, You", fontSize = 12.sp, color = Color.Gray)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Menu") }
    }
}

@Composable
fun UserAvatar(initial: String, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
