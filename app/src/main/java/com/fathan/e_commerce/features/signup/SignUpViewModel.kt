package com.fathan.e_commerce.features.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.models.auth.SignUpResult
import com.fathan.e_commerce.domain.entities.auth.AccountType
import com.fathan.e_commerce.domain.entities.auth.SignUpParams
import com.fathan.e_commerce.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val toastMessage: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    val name = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
    val accountType = MutableStateFlow(AccountType.BUYER)

    val nameError = MutableStateFlow<String?>(null)
    val emailError = MutableStateFlow<String?>(null)
    val passwordError = MutableStateFlow<String?>(null)
    val confirmPasswordError = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onAccountTypeChanged(type: AccountType) {
        accountType.value = type
    }

    private fun validate(): Boolean {
        val nameValue = name.value.trim()
        val emailValue = email.value.trim()
        val passwordValue = password.value
        val confirmValue = confirmPassword.value

        nameError.value = if (nameValue.isBlank()) "Nama tidak boleh kosong" else null

        emailError.value = when {
            emailValue.isBlank() -> "Email tidak boleh kosong"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() ->
                "Email tidak valid"
            else -> null
        }

        passwordError.value = when {
            passwordValue.isBlank() -> "Password tidak boleh kosong"
            passwordValue.length < 8 -> "Password minimal 8 karakter"
            else -> null
        }

        confirmPasswordError.value = when {
            confirmValue.isBlank() -> "Konfirmasi password tidak boleh kosong"
            confirmValue != passwordValue -> "Konfirmasi password tidak sama"
            else -> null
        }

        return listOf(
            nameError.value,
            emailError.value,
            passwordError.value,
            confirmPasswordError.value
        ).all { it == null }
    }

    fun onSignUpClicked(onSuccess: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.value = SignUpUiState(isLoading = true)

            val params = SignUpParams(
                name = name.value.trim(),
                email = email.value.trim(),
                password = password.value,
                accountType = accountType.value
            )

            when (val result = signUpUseCase(params)) {

                is SignUpResult.Success -> {
                    _uiState.value = SignUpUiState(
                        isLoading = false,
                        success = true,
                        toastMessage = "Akun berhasil dibuat!"
                    )
                    onSuccess()
                }

                is SignUpResult.Error -> {
                    _uiState.value = SignUpUiState(
                        isLoading = false,
                        success = false,
                        toastMessage = result.message
                    )
                }
            }
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }


    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
