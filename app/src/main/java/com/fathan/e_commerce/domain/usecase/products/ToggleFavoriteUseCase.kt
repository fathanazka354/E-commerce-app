package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.repository.FavoriteRepository
import javax.inject.Inject

open class ToggleFavoriteUseCase @Inject constructor(
    private val repo: FavoriteRepository
) {
    open suspend operator fun invoke(productId: Int){
        repo.toggleFavorite(productId)
    }
}