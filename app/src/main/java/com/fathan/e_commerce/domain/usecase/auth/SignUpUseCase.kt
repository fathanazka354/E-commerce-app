package com.fathan.e_commerce.domain.usecase.auth

import com.fathan.e_commerce.data.models.auth.SignUpResult
import com.fathan.e_commerce.domain.entities.auth.SignUpParams
import com.fathan.e_commerce.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(params: SignUpParams): SignUpResult {
        return repository.signUp(params)
    }
}
