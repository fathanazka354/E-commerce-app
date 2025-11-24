package com.fathan.e_commerce.ui.search

import androidx.lifecycle.ViewModel
import com.fathan.e_commerce.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject   // ➜ tambahkan ini

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {   // ➜ @Inject constructor

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _result = MutableStateFlow<List<Product>>(emptyList())
    val result: StateFlow<List<Product>> = _result

    fun updateQuery(text: String, allProducts: List<Product>) {
        _query.value = text

        _result.value =
            if (text.isBlank()) emptyList()
            else allProducts.filter {
                it.name.contains(text, ignoreCase = false) ||
                        it.brand.contains(text, false)
            }
    }
}
