package com.fathan.e_commerce.features.forgot_password

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.features.theme.BlueSoftBackground
import com.fathan.e_commerce.features.theme.TextSecondary

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val ui by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val redirectUrl = "https://reset-password-fathan.netlify.app" // must be whitelisted in Supabase

    LaunchedEffect(ui.success, ui.error) {
        ui.success?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
            viewModel.email.value = ""
        }
        ui.error?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoftBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Back button
            TextButton(onClick = onBackClick) {
                Text("Back")
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = "Forgot Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter your email address to receive a reset link and regain access to your account.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            // Card container
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.email.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Continue button
                    Button(
                        onClick = { viewModel.sendResetEmail(redirectUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !ui.isLoading
                    ) {
                        if (ui.isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Continue")
                        }
                    }

                    if (ui.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ui.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "We'll send you an email with instructions to reset your password.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}