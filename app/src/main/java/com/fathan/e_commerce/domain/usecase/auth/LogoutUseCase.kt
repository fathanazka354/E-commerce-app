package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}