package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.repository.FavoriteRepository
import javax.inject.Inject

open class IsFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    open operator fun invoke(productId: Int) = repository.isFavorite(productId)
}