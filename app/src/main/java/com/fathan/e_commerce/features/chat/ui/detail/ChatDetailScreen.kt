package com.fathan.e_commerce.features.chat.ui.detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fathan.e_commerce.domain.model.AuthUser
import com.fathan.e_commerce.features.chat.domain.entity.Message
import com.fathan.e_commerce.features.chat.ui.AudioRecorder
import com.fathan.e_commerce.features.chat.ui.components.AttachmentMenu
import com.fathan.e_commerce.features.chat.ui.components.AudioMessageContent
import com.fathan.e_commerce.features.chat.ui.components.ImageMessageContent
import com.fathan.e_commerce.features.chat.ui.components.PulsingDot
import com.fathan.e_commerce.features.chat.ui.components.RecordingOverlay
import com.fathan.e_commerce.features.chat.utils.TimeUtils
import kotlinx.coroutines.delay
import java.io.File

// ChatDetailScreen.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    viewModel: ChatDetailViewModel,
    onBackClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // ✅ Add LazyListState for auto-scroll
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // ✅ Refresh messages when screen appears
//    LaunchedEffect(Unit) {
//        viewModel.refreshMessages()
//    }


    // ✅ Auto-scroll to latest message when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    val audioRecorder = remember { AudioRecorder(context) }
    val isUploadingMedia by viewModel.isUploadingMedia.collectAsState()


    // ✅ Camera Image URI
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // ✅ Update recording duration
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(100)
                recordingDuration = audioRecorder.getDuration()
            }
        }
    }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            Log.d("ChatDetailScreen", "Image selected: $it")
            viewModel.sendImage(it)
        }
    }

    // ✅ Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            Log.d("ChatDetailScreen", "Photo captured: $capturedImageUri")
            viewModel.sendImage(capturedImageUri!!)
        }
    }

    // ✅ Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            audioRecorder.startRecording()
            isRecording = true
        } else {
            Log.e("ChatDetailScreen", "Audio permission denied")
        }
    }

    // ✅ Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create image file and launch camera
            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            capturedImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            capturedImageUri?.also {
                cameraLauncher.launch(it)
            }
        } else {
            Log.e("ChatDetailScreen", "Camera permission denied")
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        // ✅ Use Column instead of nested Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            // ✅ Recording Overlay
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

            // ✅ Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true,
            ) {
                items(
                    items = messages.reversed(),
                    key = { it.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        currentUser = currentUser,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }

            // ✅ Input Bar
            MessageInputBar(
                message = messageText,
                onMessageChange = { messageText = it },
                isRecording = isRecording,
                isSending = isSending || isUploadingMedia,
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText.trim())
                        messageText = ""
                    }
                },
                onImageClick = {
                    // Launch image picker
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCameraClick = {
                    // Check camera permission
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Permission already granted
                            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            capturedImageUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            capturedImageUri?.also {
                                cameraLauncher.launch(it)

                            }
                        }
                        else -> {
                            // Request permission
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                onStartRecording = {
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            audioRecorder.startRecording()
                            isRecording = true
                        }
                        else -> {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onStopRecording = {
                    val result = audioRecorder.stopRecording()
                    isRecording = false

                    result?.let { (file, duration) ->
                        file?.let {
                            val uri = Uri.fromFile(it)
                            viewModel.sendAudio(uri, duration)
                        }
                    }

                    recordingDuration = 0L
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageBubble(
    message: Message,
    currentUser: AuthUser?,
    modifier: Modifier = Modifier
) {
    val isCurrentUser = message.senderId == currentUser?.uid

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser)
            Arrangement.End
        else
            Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isCurrentUser) 12.dp else 4.dp,
                topEnd = if (isCurrentUser) 4.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            color = if (isCurrentUser)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // ✅ Check message type and display accordingly
                when (message.messageType) {
                    "image" -> {
                        ImageMessageContent(
                            imageData = message.messageContent,
                            isCurrentUser = isCurrentUser
                        )
                    }
                    "audio" -> {
                        AudioMessageContent(
                            audioData = message.messageContent,
                            isCurrentUser = isCurrentUser
                        )
                    }
                    else -> {
                        // Text message
                        Text(
                            text = message.messageContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentUser)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = TimeUtils.getSmartTime(createdAt = message.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCurrentUser)
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead)
                                Icons.Default.DoneAll
                            else
                                Icons.Default.Done,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            modifier = Modifier.size(16.dp),
                            tint = if (message.isRead)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    isRecording: Boolean,
    isSending: Boolean,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // ✅ Attachment Menu (Expandable)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                AttachmentMenu(
                    onImageClick = {
                        onImageClick()
                        isExpanded = false
                    },
                    onCameraClick = {
                        onCameraClick()
                        isExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ✅ Main Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ✅ Attach Button (Gallery/Camera)
                if (!isRecording) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        enabled = !isSending,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = if (isExpanded)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // ✅ Text Input Field
                if (!isRecording) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = onMessageChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        placeholder = {
                            Text(
                                "Message",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        enabled = !isSending,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 5,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // ✅ Recording Indicator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pulsing dot animation
                            PulsingDot()

                            Text(
                                "Recording...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // ✅ Send/Mic Button
                AnimatedContent(
                    targetState = when {
                        isSending -> "loading"
                        isRecording -> "stop"
                        message.isNotBlank() -> "send"
                        else -> "mic"
                    },
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "button_animation"
                ) { state ->
                    when (state) {
                        "loading" -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        "stop" -> {
                            FloatingActionButton(
                                onClick = onStopRecording,
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop Recording",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        "send" -> {
                            FloatingActionButton(
                                onClick = onSendClick,
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        "mic" -> {
                            FloatingActionButton(
                                onClick = onStartRecording,
                                modifier = Modifier
                                    .size(48.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                onStartRecording()
                                                tryAwaitRelease()
                                                onStopRecording()
                                            }
                                        )
                                    },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Record Audio",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}