package com.fathan.e_commerce.ui.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.repository.AuthResult
import com.fathan.e_commerce.domain.usecase.auth.RequestPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase
) : ViewModel() {
    val email = MutableStateFlow("")
    private val _ui = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _ui

    fun sendResetEmail(redirectTo: String? = null) {
        val e = email.value.trim()
        if (e.isEmpty()) {
            _ui.value = ForgotUiState(error = "Email tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _ui.value = ForgotUiState(isLoading = true)
            val r = try { requestPasswordResetUseCase(e, redirectTo) } catch (t: Throwable) {
                AuthResult.Error(t.message.toString()) }
            if (r is AuthResult.Success) _ui.value = ForgotUiState(success = "Cek email Anda untuk instruksi")
            else _ui.value = ForgotUiState(error = "Gagal mengirim email")
        }
    }
}

data class ForgotUiState(val isLoading: Boolean = false, val success: String? = null, val error: String? = null)
