package com.fathan.e_commerce.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val prefs: UserPreferences
): ViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    val emailError = MutableStateFlow<String?>(null)
    val passwordError = MutableStateFlow<String?>(null)

    fun validate(): Boolean {
        val emailValue = email.value
        val passwordValue = password.value

        emailError.value = when{
            emailValue.isEmpty() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> "Invalid email format"
            else -> null
        }

        passwordError.value = when {
            passwordValue.isEmpty() -> "Password cannot be empty"
            passwordValue.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }

        return emailError.value == null && passwordError.value == null
    }

    fun login(onSuccess: () -> Unit){
        if (!validate()) return

        viewModelScope.launch {
            prefs.saveUser(
                name = email.value.substringBefore("@"),
                email = email.value
            )
            onSuccess()
        }
    }

}
