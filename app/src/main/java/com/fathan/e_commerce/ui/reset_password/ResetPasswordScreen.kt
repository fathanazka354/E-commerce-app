package com.fathan.e_commerce.ui.reset_password

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ResetPasswordScreen(
    accessToken: String,
    refreshToken: String,
    onDone: () -> Unit,
    onBackToLoginClick: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val pw by viewModel.password.collectAsState()
    val cpw by viewModel.confirmPassword.collectAsState()
    val ui by viewModel.uiState.collectAsState()

    // Set tokens when screen loads
    LaunchedEffect(accessToken) {
        if (accessToken.isNotEmpty()) {
            viewModel.setTokens(accessToken, refreshToken)
            Log.d("ResetPasswordScreen", "Tokens set in ViewModel")
        } else {
            Log.e("ResetPasswordScreen", "No access token provided!")
        }
    }

    LaunchedEffect(ui.successMessage) {
        ui.successMessage?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
            delay(1000)
            onDone()
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
            TextButton(onClick = onBackToLoginClick) {
                Text("Back")
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = "Reset Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter your new password. Password must be at least 8 characters long.",
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
                    // New password field
                    OutlinedTextField(
                        value = pw,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("New password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Confirm password field
                    OutlinedTextField(
                        value = cpw,
                        onValueChange = { viewModel.onConfirmPasswordChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Confirm password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(Modifier.height(24.dp))

                    // Set password button
                    Button(
                        onClick = { viewModel.submit(onSuccess = {}) },
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
                            Text("Set new password")
                        }
                    }

                    if (ui.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ui.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Your password will be updated and you can log in with your new credentials.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Back to login
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Remember your password? ",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Back to Login",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBackToLoginClick() }
                )
            }
        }
    }
}
