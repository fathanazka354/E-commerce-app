package com.fathan.e_commerce.features.signup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.fathan.e_commerce.domain.entities.auth.AccountType
import com.fathan.e_commerce.features.theme.BlueSoftBackground
import com.fathan.e_commerce.features.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onAlreadyHaveAccountClick: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val accountType by viewModel.accountType.collectAsState()

    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    var accountTypeExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
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
            TextButton(onClick = onBackClick) {
                Text("Back")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sign up",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Create an account to start shopping with ShopEase.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

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

                    // Full name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            viewModel.name.value = it
                        },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        singleLine = true
                    )
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            viewModel.email.value = it
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        singleLine = true
                    )
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            viewModel.password.value = it
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError != null,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Confirm password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            viewModel.confirmPassword.value = it
                        },
                        label = { Text("Confirm password") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmPasswordError != null,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (confirmPasswordError != null) {
                        Text(
                            text = confirmPasswordError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Account type dropdown (Buyer / Seller)
                    ExposedDropdownMenuBox(
                        expanded = accountTypeExpanded,
                        onExpandedChange = { accountTypeExpanded = !accountTypeExpanded }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            value = when (accountType) {
                                AccountType.BUYER -> "Buyer"
                                AccountType.SELLER -> "Seller"
                            },
                            onValueChange = {},
                            label = { Text("Account type") },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = accountTypeExpanded
                                )
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = accountTypeExpanded,
                            onDismissRequest = { accountTypeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Buyer") },
                                onClick = {
                                    viewModel.onAccountTypeChanged(AccountType.BUYER)
                                    accountTypeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Seller") },
                                onClick = {
                                    viewModel.onAccountTypeChanged(AccountType.SELLER)
                                    accountTypeExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Sign up button
                    Button(
                        onClick = { viewModel.onSignUpClicked(onSignUpSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Create account")
                        }
                    }

                    if (uiState.errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "By signing up, you agree to our Terms & Conditions and Privacy Policy.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Login",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onAlreadyHaveAccountClick() }
                )
            }
        }
    }
}
