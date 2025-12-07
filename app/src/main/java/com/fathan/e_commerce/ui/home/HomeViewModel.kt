package com.fathan.e_commerce.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.repository.ProductFilter
import com.fathan.e_commerce.domain.usecase.products.GetCategoriesUseCase
import com.fathan.e_commerce.domain.usecase.products.GetFlashSaleUseCase
import com.fathan.e_commerce.domain.usecase.products.GetProductsUseCaseV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
//        viewModelScope.launch {
//            // load categories
//            when (val res = getCategoriesUseCase()) {
//                is Result.Success -> {} // not used — kotlin Result wrapper isn't sealed, so below style:
//            }
//        }

        viewModelScope.launch {
            val cats = getCategoriesUseCase()
            Log.d("HomeViewModel", "loadInitialData: ${cats.getOrNull()?.size}")
            if (cats.isSuccess) _categories.value = cats.getOrNull().orEmpty()
            else {
                // handle error (log / emit state)
            }

            val prods = getProductsUseCase(ProductFilter(limit = 20))
            if (prods.isSuccess) _products.value = prods.getOrNull().orEmpty()

            val flashes = getFlashSaleUseCase()
            if (flashes.isSuccess) _flashDeals.value = flashes.getOrNull().orEmpty()
        }
    }

    // public API to load products with filter (category, query, seller etc)
    fun loadProductsWithFilter(filter: com.fathan.e_commerce.domain.repository.ProductFilter) {
        viewModelScope.launch {
            val res = getProductsUseCase(filter)
            if (res.isSuccess) _products.value = res.getOrNull().orEmpty()
            else {
                // handle error (log / emit state)
            }
        }
    }
}
