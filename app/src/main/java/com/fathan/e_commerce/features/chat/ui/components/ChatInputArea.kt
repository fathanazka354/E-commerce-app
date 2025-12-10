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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.View
import androidx.compose.ui.graphics.Color

/**
 * Chat input area used on ChatDetailScreen.
 *
 * Callbacks:
 *  - onSendText(text)
 *  - onAttachImage() -> launch image picker from caller
 *  - onStartRecording() -> start UI audio recorder (handled in UI)
 *  - onStopRecordingAndSend() -> stop recorder and send recorded file
 *
 * Notes:
 * - This composable intentionally does not perform the recording itself — keep audio recording infra in the screen / recorder class.
 */
@Composable
fun ChatInputArea(
    onSendText: (String) -> Unit,
    onAttachImage: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecordingAndSend: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    val view: View = LocalView.current
    val ctx = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = if (isRecording) "Recording..." else text,
                    onValueChange = { if (!isRecording) text = it },
                    enabled = !isRecording,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(fontSize = 16.sp, color = if (isRecording) Color.Red else Color.Black),
                    decorationBox = { inner ->
                        if (text.isBlank() && !isRecording) {
                            Text("Type here..", color = Color.LightGray, fontSize = 16.sp)
                        }
                        inner()
                    }
                )

                if (!isRecording) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onAttachImage, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Image, contentDescription = "Attach image")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { /* camera not handled here */ }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = CircleShape,
            color = if (isRecording) Color.Red else Color(0xFF5C6BC0),
            tonalElevation = 4.dp,
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
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                } else {
                                    Toast.makeText(ctx, "Hold to record voice message", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onLongPress = {
                                isRecording = true
                                onStartRecording()
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                            },
                            onPress = {
                                try {
                                    awaitRelease()
                                    if (isRecording) {
                                        isRecording = false
                                        onStopRecordingAndSend()
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                    }
                                } catch (_: Exception) { /* ignore */ }
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
