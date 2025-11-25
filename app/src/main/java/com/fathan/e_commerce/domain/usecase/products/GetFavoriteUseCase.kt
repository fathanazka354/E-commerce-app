package com.fathan.e_commerce.domain.usecase.products

import com.fathan.e_commerce.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repo: FavoriteRepository
) {
    operator fun invoke() = repo.getFavorites()
}
