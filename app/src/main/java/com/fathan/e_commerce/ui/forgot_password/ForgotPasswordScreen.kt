package com.fathan.e_commerce.ui.forgot_password

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val ui by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current
    val redirectDeepLink = "myapp://reset-password" // must be whitelisted in Supabase

    LaunchedEffect(ui.success, ui.error) {
        ui.success?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); viewModel.email.value = "" }
        ui.error?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show() }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Forgot Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = { viewModel.email.value = it }, label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.sendResetEmail(redirectDeepLink) }, enabled = !ui.isLoading) {
            if (ui.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp))
            else Text("Send reset email")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackClick) { Text("Back") }
    }
}
