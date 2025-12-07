package com.fathan.e_commerce.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.domain.usecase.products.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _result = MutableStateFlow<List<Product>>(emptyList())
    val result: StateFlow<List<Product>> = _result

    fun updateQuery(text: String, sellerId: Long? = null) {
        _query.value = text
        if (text.isBlank()) {
            _result.value = emptyList()
            return
        }

        viewModelScope.launch {
            val res = searchProductsUseCase(text, sellerId)
            if (res.isSuccess) _result.value = res.getOrNull().orEmpty()
            else _result.value = emptyList()
        }
    }
}
