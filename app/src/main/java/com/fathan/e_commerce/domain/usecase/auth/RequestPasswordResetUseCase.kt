package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, redirectTo: String? = null) = repo.requestPasswordReset(email, redirectTo)
}
