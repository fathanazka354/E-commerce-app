package com.fathan.e_commerce.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.entities.product.ProductFilter
import com.fathan.e_commerce.domain.usecase.products.GetCategoriesUseCase
import com.fathan.e_commerce.domain.usecase.products.GetFlashSaleUseCase
import com.fathan.e_commerce.domain.usecase.products.GetProductsUseCaseV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCaseV2,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getFlashSaleUseCase: GetFlashSaleUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _flashDeals = MutableStateFlow<List<FlashSaleWithProduct>>(emptyList())
    val flashDeals: StateFlow<List<FlashSaleWithProduct>> = _flashDeals

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            val cats = getCategoriesUseCase()
            Log.d("HomeViewModel", "loadInitialData: ${cats.getOrNull()?.size}")
            if (cats.isSuccess) _categories.value = cats.getOrNull().orEmpty()
            
            // Load all products initially
            loadProductsWithFilter(ProductFilter(limit = 20))

            val flashes = getFlashSaleUseCase()
            if (flashes.isSuccess) _flashDeals.value = flashes.getOrNull().orEmpty()

            _isLoading.value = false
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
        val filter = if (category != null) {
            ProductFilter(categoryId = category.id, limit = 20)
        } else {
            ProductFilter(limit = 20)
        }
        loadProductsWithFilter(filter)
    }

    // public API to load products with filter (category, query, seller etc)
    fun loadProductsWithFilter(filter: ProductFilter) {
        viewModelScope.launch {
            _isLoading.value = true

            val res = getProductsUseCase(filter)
            if (res.isSuccess) _products.value = res.getOrNull().orEmpty()
            else {
                // handle error (log / emit state)
            }
            _isLoading.value = false

        }
    }

    fun refresh(category: Category?) {
        // public API to refresh whole page
        viewModelScope.launch {
            _selectedCategory.value = category
            loadProductsWithFilter(
                ProductFilter(
                    categoryId = category?.id
                )
            )
        }
    }

}
