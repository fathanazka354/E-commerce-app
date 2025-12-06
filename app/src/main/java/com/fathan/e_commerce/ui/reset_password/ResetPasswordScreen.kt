// ui/reset_password/ResetPasswordScreen.kt
package com.fathan.e_commerce.ui.reset_password

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ResetPasswordScreen(
    token: String?,
    onDone: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current
    val pw by viewModel.password.collectAsState()
    val cpw by viewModel.confirmPassword.collectAsState()
    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(ui.successMessage) {
        ui.successMessage?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_LONG).show()
            // navigate back or to login
            onDone()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Reset Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Silakan masukkan password baru. Password minimal 8 karakter.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = pw,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("New password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cpw,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        ui.errorMessage?.let { err ->
            Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.submit(onSuccess = { /* optional callback */ }) },
            enabled = !ui.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (ui.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else Text("Set new password")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { /* maybe open web fallback or nav back */ }) {
            Text("Back to Login")
        }
    }
}
