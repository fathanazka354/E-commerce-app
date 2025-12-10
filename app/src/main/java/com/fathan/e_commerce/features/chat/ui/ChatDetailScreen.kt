package com.fathan.e_commerce.features.chat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.SoundEffectConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import com.fathan.e_commerce.features.chat.ui.components.ChatInputArea
import com.fathan.e_commerce.features.chat.ui.components.MessageItem

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatDetailScreen(
    roomId: String,
    myAuthId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // set current room
    LaunchedEffect(roomId, myAuthId) {
        viewModel.setRoom(roomId, myAuthId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val messages = uiState.messages
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val view = LocalView.current

    // Image picker
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) viewModel.sendImage(uri)
        }
    )

    // Audio permission
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasAudioPermission = granted }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        bottomBar = {
            ChatInputArea(
                onSendText = { txt -> viewModel.sendText(txt); view.playSoundEffect(SoundEffectConstants.CLICK) },
                onAttachImage = { singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onStartRecording = {
                    if (hasAudioPermission) {
                        // UI layer should handle recorder start; provide lightweight hint: launch recording handled elsewhere
                        // For your existing AudioRecorder approach, manage it in the Composable scope
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecordingAndSend = {
                    // you need to supply Uri+duration from the AudioRecorder in UI; assume ChatDetailScreen integrates AudioRecorder and calls viewModel.sendAudio(...)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message)
            }
        }
    }
}
