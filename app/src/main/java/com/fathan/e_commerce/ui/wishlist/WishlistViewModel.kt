package com.fathan.e_commerce.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.local.WishlistCollectionEntity
import com.fathan.e_commerce.data.local.WishlistItemEntity
import com.fathan.e_commerce.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val collections: List<CollectionUiModel> = emptyList(),
    val showBottomSheet: Boolean = false,
    val activeProductToAdd: ProductWishlistUi? = null
)

data class CollectionUiModel(
    val id: Int,
    val name: String,
    val itemCount: Int,
    val thumbnail: String?
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    val collections: StateFlow<List<WishlistCollectionEntity>> = wishlistRepository.getCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadCollectionsUiData()
        createDefaultCollection()
    }

    private fun createDefaultCollection() {
        viewModelScope.launch {
            try {
                wishlistRepository.createCollection("Semua Wishlist")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCollectionsUiData() {
        viewModelScope.launch {
            wishlistRepository.getCollections().collect { entities ->
                val uiModels = entities.map { entity ->
                    val previewImage = wishlistRepository.getPreviewImage(entity.id)
                    val count = wishlistRepository.getItemCount(entity.id)

                    CollectionUiModel(
                        id = entity.id,
                        name = entity.name,
                        itemCount = count,
                        thumbnail = previewImage
                    )
                }
                _uiState.value = _uiState.value.copy(collections = uiModels)
            }
        }
    }


    fun isProductWishlisted(productId: Int): Flow<Boolean> {
        return wishlistRepository.isProductInWishlist(productId)
    }

    fun addToCollection(product: ProductWishlistUi, collectionId: Int) {
        viewModelScope.launch {
            wishlistRepository.addItemToCollection(
                WishlistItemEntity(
                    productId = product.id,
                    collectionId = collectionId,
                    name = product.name,
                    price = product.price,
                    imageUrl = product.imageUrl,
//                    originalPrice = product.originalPrice,
//                    discount = product.discount,
//                    shopName = product.shopName,
//                    location = product.location,
//                    rating = product.rating,
//                    soldCount = product.soldCount,
//                    isOfficial = product.isOfficial
                )
            )
        }
    }

    fun removeFromWishlist(productId: Int) {
        viewModelScope.launch {
            wishlistRepository.deleteItem(productId)
        }
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                wishlistRepository.createCollection(name)
            }
        }
    }

    fun updateCollectionName(id: Int, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                wishlistRepository.updateCollectionName(id, newName)
            }
        }
    }

    fun deleteCollection(id: Int) {
        viewModelScope.launch {
            wishlistRepository.deleteCollection(id)
        }
    }

    fun getItemsByCollection(collectionId: Int): Flow<List<WishlistItemEntity>> {
        return wishlistRepository.getItemsByCollection(collectionId)
    }

    fun onLoveClick(product: ProductWishlistUi) {
        _uiState.value = _uiState.value.copy(
            showBottomSheet = true,
            activeProductToAdd = product
        )
    }

    fun dismissBottomSheet() {
        _uiState.value = _uiState.value.copy(
            showBottomSheet = false,
            activeProductToAdd = null
        )
    }
}
