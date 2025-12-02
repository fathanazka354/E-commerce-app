package com.fathan.e_commerce.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.domain.repository.AuthResult
import com.fathan.e_commerce.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
data class LoginUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userPreferences: UserPreferences
): ViewModel() {
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    val emailError = MutableStateFlow<String?>(null)
    val passwordError = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: MutableStateFlow<LoginUIState> = _uiState

    fun onLoginClickedUp(onSuccess: () -> Unit){
        val emailValue = email.value.trim()
        val passwordValue = password.value

        emailError.value = when {
            emailValue.isBlank() -> "Email tidak boleh kosong"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> "Email tidak valid"
            else -> null
        }

        passwordError.value = when {
            emailValue.isBlank() -> "Password tidak boleh kosong"
            passwordValue.length < 8 -> "Password minimal 8 karakter"
            else -> null
        }

        if (emailError.value != null && passwordError.value != null) return

        viewModelScope.launch {
            _uiState.value = LoginUIState(isLoading = true)

            when (val result = loginUseCase(emailValue, passwordValue)) {
                is AuthResult.Success -> {
                    userPreferences.setIsLoggedIn(true)
                    _uiState.value = LoginUIState(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUIState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

            }
        }
    }

    fun clearError(){
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

//    fun validate(): Boolean {
//        val emailValue = email.value
//        val passwordValue = password.value
//
//        emailError.value = when{
//            emailValue.isEmpty() -> "Email cannot be empty"
//            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> "Invalid email format"
//            else -> null
//        }
//
//        passwordError.value = when {
//            passwordValue.isEmpty() -> "Password cannot be empty"
//            passwordValue.length < 8 -> "Password must be at least 8 characters"
//            else -> null
//        }
//
//        return emailError.value == null && passwordError.value == null
//    }
//
//    fun login(onSuccess: () -> Unit){
//        if (!validate()) return
//
//        viewModelScope.launch {
//            prefs.saveUser(
//                name = email.value.substringBefore("@"),
//                email = email.value
//            )
//            onSuccess()
//        }
//    }

}
