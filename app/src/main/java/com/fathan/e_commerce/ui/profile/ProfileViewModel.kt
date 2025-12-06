package com.fathan.e_commerce.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val logoutUser: LogoutUseCase
): ViewModel() {

    val name: StateFlow<String> = prefs.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    val email: StateFlow<String> = prefs.userEmailFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    /**
     * Logout dengan penanganan error yang aman
     * Menggunakan withContext(NonCancellable) untuk memastikan
     * prefs.logout() tetap dipanggil meskipun ViewModel dibersihkan
     */
    fun logout(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoggingOut.value = true

                withContext(NonCancellable) {
                    try {
                        val response = logoutUser()

                        prefs.logout()

                        if (response) {
                            Log.d("ProfileViewModel", "Logout successful")
                            onSuccess()
                        } else {
                            Log.w("ProfileViewModel", "Logout API returned false, but preferences cleared")
                            // Tetap anggap sukses karena preferences sudah dibersihkan
                            onSuccess()
                        }
                    } catch (e: CancellationException) {
                        Log.d("ProfileViewModel", "Logout cancelled, clearing preferences")
                        prefs.logout()
                        onSuccess()
                    } catch (e: Exception) {
                        Log.e("ProfileViewModel", "Logout error", e)
                        prefs.logout()
                        onError(e.message ?: "Logout Failed")
                    }
                }
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ProfileViewModel", "ViewModel cleared")
    }
}