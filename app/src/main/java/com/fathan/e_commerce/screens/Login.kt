package com.fathan.e_commerce.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    userPreferences: UserPreferences

) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }


    val scope = rememberCoroutineScope()


    fun validate(): Boolean{
        emailError = when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
            else -> null
        }

        passwordError = when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        return emailError == null && passwordError == null
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoftBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Welcome to",
                fontSize = 18.sp,
                color = TextSecondary
            )
            Text(
                text = "ShopEase",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sign in to discover the latest deals on gadgets & accessories.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
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
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            validate()
                                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = emailError != null,
                        label = { Text("Email") }
                    )
                    if (emailError != null) {
                        Text(emailError!!, color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) validate()
                        },
                        label = { Text("Password") },
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (validate()) {
                                scope.launch {
                                    userPreferences.saveUser(
                                        name = email.substringBefore("@"),
                                        email = email
                                    )
                                }
                                onLoginSuccess()
                            }
                        }, // no real auth, just demo
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Login")
                    }

                    Text(
                        text = "By continuing you agree to our Terms & Privacy Policy.",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}