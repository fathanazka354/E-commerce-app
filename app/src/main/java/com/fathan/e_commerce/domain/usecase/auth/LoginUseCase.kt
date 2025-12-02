package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) = authRepository.login(email = email, password = password)
}