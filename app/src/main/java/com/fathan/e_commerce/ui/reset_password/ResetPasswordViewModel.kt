package com.fathan.e_commerce.ui.reset_password

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.repository.AuthRepository
import com.fathan.e_commerce.domain.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _accessToken = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(ResetPasswordUIState())
    val uiState: StateFlow<ResetPasswordUIState> = _uiState

    // Call this when you extract tokens from URL
    fun setTokens(accessToken: String, refreshToken: String) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        Log.d("ResetPasswordVM", "Tokens set - access token length: ${accessToken.length}")
    }

    fun onPasswordChange(value: String) {
        password.value = value
    }

    fun onConfirmPasswordChange(value: String) {
        confirmPassword.value = value
    }

    fun submit(onSuccess: () -> Unit) {
        val pw = password.value
        val cpw = confirmPassword.value
        val token = _accessToken.value

        // Validation
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Invalid or expired reset link"
            )
            return
        }

        if (pw.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password tidak boleh kosong"
            )
            return
        }

        if (pw.length < 8) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password minimal 8 karakter"
            )
            return
        }

        if (pw != cpw) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password tidak cocok"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = authRepository.resetPasswordWithToken(token, pw)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Password berhasil diubah! Silakan login."
                    )
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

data class ResetPasswordUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
