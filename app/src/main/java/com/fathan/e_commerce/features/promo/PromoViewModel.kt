package com.fathan.e_commerce.features.promo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.data.models.PromoProduct
import com.fathan.e_commerce.data.models.PromoVoucher
import com.fathan.e_commerce.domain.repository.PromoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromoViewModel @Inject constructor(
    private val repository: PromoRepository
) : ViewModel() {

    // State Data
    private val _promoProducts = MutableStateFlow<List<PromoProduct>>(emptyList())
    val promoProducts: StateFlow<List<PromoProduct>> = _promoProducts.asStateFlow()

    private val _localProducts = MutableStateFlow<List<PromoProduct>>(emptyList())
    val localProducts: StateFlow<List<PromoProduct>> = _localProducts.asStateFlow()

    private val _flashSaleProducts = MutableStateFlow<List<PromoProduct>>(emptyList())
    val flashSaleProducts: StateFlow<List<PromoProduct>> = _flashSaleProducts.asStateFlow()

    private val _vouchers = MutableStateFlow<List<PromoVoucher>>(emptyList())
    val vouchers: StateFlow<List<PromoVoucher>> = _vouchers.asStateFlow()

    // State Filter Kategori
    private val _selectedCategory = MutableStateFlow("Semua Promo")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val categories = listOf("Semua Promo", "Makanan & Minuman", "Kesehatan", "Elektronik", "Fashion", "Olahraga")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getPromoProducts().collect { _promoProducts.value = it }
        }
        viewModelScope.launch {
            repository.getLocalProducts().collect { _localProducts.value = it }
        }
        viewModelScope.launch {
            repository.getFlashSaleProducts().collect { _flashSaleProducts.value = it }
        }
        viewModelScope.launch {
            repository.getVouchers().collect { _vouchers.value = it }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getFilteredPromoProducts(): List<PromoProduct> {
        return if (_selectedCategory.value == "Semua Promo") {
            _promoProducts.value
        } else {
            _promoProducts.value.filter { it.category == _selectedCategory.value }
        }
    }
}
