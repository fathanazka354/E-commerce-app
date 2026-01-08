package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.currentUser()
}