// ui/reset_password/ResetPasswordViewModel.kt
package com.fathan.e_commerce.ui.reset_password

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.repository.AuthResult
import com.fathan.e_commerce.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResetUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val token: String? = savedStateHandle.get<String>("token")

    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _uiState = MutableStateFlow(ResetUiState())
    val uiState: StateFlow<ResetUiState> = _uiState

    fun onPasswordChange(v: String) { password.value = v }
    fun onConfirmPasswordChange(v: String) { confirmPassword.value = v }

    fun submit(onSuccess: () -> Unit = {}) {
        // local validation
        if (token.isNullOrBlank()) {
            _uiState.value = ResetUiState(errorMessage = "Token tidak ditemukan. Buka link dari email yang sama.")
            return
        }

        val p = password.value.trim()
        val cp = confirmPassword.value.trim()

        if (p.length < 8) {
            _uiState.value = ResetUiState(errorMessage = "Password minimal 8 karakter")
            return
        }
        if (p != cp) {
            _uiState.value = ResetUiState(errorMessage = "Password dan konfirmasi tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResetUiState(isLoading = true)
            resetPasswordUseCase(token, p).apply {
                Log.d("TAG", "resetPasswordWithToken: HAHAH $this")
                if (this is AuthResult.Success) {
                    _uiState.value = ResetUiState(successMessage = "Password berhasil diubah")
                    // optional callback to navigate away
                    onSuccess()
                } else {
                    _uiState.value = ResetUiState(errorMessage = "Gagal memperbarui password")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
