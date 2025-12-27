package com.fathan.e_commerce.features.chat.ui.components

import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing) // ← Safe area global
            .background(Color(0xFFF9F9F9)),
        color = Color.Transparent,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = if (isRecording) "Recording..." else text,
                        onValueChange = { if (!isRecording) text = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onAttachImage) {
                        Icon(Icons.Default.Image, contentDescription = "Attach image")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = { /* camera TODO */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = if (isRecording) Color.Red else Color(0xFF5C6BC0),
                modifier = Modifier.size(52.dp),
                shadowElevation = 6.dp
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (text.isNotBlank()) {
                                    onSendText(text.trim())
                                    text = ""
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                } else {
                                    Toast.makeText(context, "Hold to record voice message", Toast.LENGTH_SHORT).show()
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
                    },
                    contentAlignment = Alignment.Center
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
}
