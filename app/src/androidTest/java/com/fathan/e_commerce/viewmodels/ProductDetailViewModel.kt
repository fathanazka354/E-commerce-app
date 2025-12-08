package com.fathan.e_commerce.viewmodels

import com.fathan.e_commerce.domain.usecase.products.IsFavoriteUseCase
import com.fathan.e_commerce.domain.usecase.products.ToggleFavoriteUseCase
import com.fathan.e_commerce.features.product.ui.ProductDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ProductDetailViewModelTest {

    private lateinit var toggleFavorite: ToggleFavoriteUseCase
    private lateinit var isFavoriteUseCase: IsFavoriteUseCase
    private lateinit var vm: ProductDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())

        toggleFavorite = mock(ToggleFavoriteUseCase::class.java)
        isFavoriteUseCase = mock(IsFavoriteUseCase::class.java)

        vm = ProductDetailViewModel(toggleFavorite, isFavoriteUseCase)
    }

    @Test
    fun increase_quantity_should_work() {
        assertEquals(1, vm.quantity.value)
        vm.increaseQuantity()
        assertEquals(2, vm.quantity.value)
    }

    @Test
    fun decrease_quantity_should_not_go_below_0() {
        vm.decreaseQuantity()
        assertEquals(0, vm.quantity.value)
    }

    @Test
    fun toggleFavorite_should_trigger_usecase() = runTest {
        vm.toggle(10)
        advanceUntilIdle()

        verify(toggleFavorite).invoke(10)
    }

    @Test
    fun isFavorite_should_return_correct_flow() = runTest {
        `when`(isFavoriteUseCase.invoke(10)).thenReturn(flowOf(true))

        val result = vm.isFavorite(10)

        assertEquals(true, result.first())
    }
}
