package com.fathan.e_commerce.features.product.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fathan.e_commerce.features.product.ui.components.BottomBuyBar
import com.fathan.e_commerce.features.product.ui.components.DescriptionSection
import com.fathan.e_commerce.features.product.ui.components.FeedbackSection
import com.fathan.e_commerce.features.product.ui.components.ProductDetailShimmer
import com.fathan.e_commerce.features.product.ui.components.ProductHeaderSection
import com.fathan.e_commerce.features.product.ui.components.ProductImageCarousel
import com.fathan.e_commerce.features.product.ui.components.RecommendedSection
import com.fathan.e_commerce.features.product.ui.components.VariantSelector

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onAddToCart: (productId: Int, variantId: Int?, qty: Int) -> Unit,
    onBuyWithPromo: (productId: Int, variantId: Int?, qty: Int) -> Unit,
    onOpenFeedback: (productId: Int) -> Unit,
    onOpenProduct: (productId: Int) -> Unit
) {

    val scrollState = rememberScrollState()
    var selectedVariant by remember { mutableStateOf<Int?>(null) }
    val quantity by viewModel.quantity.collectAsState()

//    LaunchedEffect(uiState.product.id) {
//        viewModel.loadProduct(uiState.product.id)
//    }

    Scaffold(
        modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),
        bottomBar = {
            if (!uiState.loading) {
                BottomBuyBar(
                    quantity = quantity,
                    productId = uiState.product.id,
                    variantId = selectedVariant,
                    onAddToCart = onAddToCart,
                    onBuy = onBuyWithPromo
                )
            }
        }
    ) { padding ->

        if (uiState.loading) {
            ProductDetailShimmer()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(paddingValues = padding)
        ) {
            ProductImageCarousel(
                images = uiState.images,
                onBack = onBack,
                isFavorite = uiState.isFavorite,
                onFavorite = { viewModel.toggleFavorite(uiState.product.id) }
            )

            ProductHeaderSection(uiState)

            VariantSelector(
                variants = uiState.variants,
                selected = selectedVariant,
                onSelect = { selectedVariant = it }
            )

            DescriptionSection(uiState)

            FeedbackSection(
                avg = uiState.avgRating,
                total = uiState.reviewCount,
                onClick = { onOpenFeedback(uiState.product.id) }
            )

            RecommendedSection(
                list = uiState.recommended,
                onOpenProduct = onOpenProduct
            )

            Spacer(Modifier.height(110.dp))
        }
    }
}

