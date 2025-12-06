package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String) = repo.resetPasswordWithToken(token, newPassword)
}