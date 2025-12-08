package com.fathan.e_commerce.features.product.ui
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.models.ProductDetailAggregate
import com.fathan.e_commerce.data.models.RecommendedDto
import com.fathan.e_commerce.domain.entities.product.FlashSaleItem
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.entities.product.ProductVariant
import com.fathan.e_commerce.domain.usecase.products.GetProductDetailUseCase
import com.fathan.e_commerce.domain.usecase.products.IsFavoriteUseCase
import com.fathan.e_commerce.domain.usecase.products.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState(loading = true))
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var currentLoadJob: Job? = null


    /**
     * Load product detail by id. Cancels previous load if still running.
     */
    fun loadProduct(productId: Int, forceRefresh: Boolean = false) {
        // cancel previous
        Log.d("ProductViewModel", "loadProduct: ${productId}")
        currentLoadJob?.cancel()
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copyLoading()

            try {
                val agg: ProductDetailAggregate = getProductDetailUseCase(productId)
                // map aggregate -> ui state
                val productDomain: Product = agg.product
                val images = agg.images.ifEmpty {
                    // fallback to thumbnail if any (or empty)
                    agg.recommended.firstOrNull()?.thumbnail?.let { listOf(it) } ?: emptyList()
                }
                val variantsDomain: List<ProductVariant> = agg.variants.map { dto ->
                    ProductVariant(
                        id = dto.id,
                        name = dto.name ?: "",
                        price = (dto.price?.toDouble() ?: 0.0).toLong(),
                        stock = dto.stock ?: 0
                    )
                }

                // map flashSale dto (if repository returned FlashSaleDto -> repo already converted to FlashSaleDto)
                val flashSaleDomain: FlashSaleItem? = agg.flashSale?.let { fs ->
                    FlashSaleItem(
                        id = fs.id,
                        productId = fs.product_id.toInt(),
                        flashPrice = (fs.flash_price ?: 0.0),
                        originalPrice = (fs.original_price ?: 0.0),
                        stock = fs.flash_stock ?: 0L,
                        sold = (fs.sold_qty ?: 0.0).toInt()
                    )
                }

                // recommended are already RecommendedDto objects from repository
                val recommended: List<RecommendedDto> = agg.recommended

                val isFav = isFavoriteUseCase(productDomain.id)

                _uiState.value = ProductDetailUiState(
                    loading = false,
                    error = null,
                    product = productDomain,
                    images = images,
                    variants = variantsDomain,
                    flashSale = flashSaleDomain,
                    avgRating = agg.avgRating,
                    reviewCount = agg.reviewCount,
                    recommended = recommended,
                    isFavorite = isFav.first()
                )

            } catch (t: Throwable) {
                // In case of transient error, you might want to retry simple times (optional)
                _uiState.value = _uiState.value.copyError(t.message ?: "Gagal memuat produk")
            }
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase(productId)
            // update ui state
            val cur = _uiState.value
            _uiState.value = cur.copy(isFavorite = !cur.isFavorite)
        }
    }

    fun retryLoad(productId: Int) {
        loadProduct(productId, forceRefresh = true)
    }

    // quantity functions (re-use if earlier present)
    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()
    fun increaseQuantity() { _quantity.value = _quantity.value + 1 }
    fun decreaseQuantity() { if (_quantity.value > 1) _quantity.value = _quantity.value - 1 }
}