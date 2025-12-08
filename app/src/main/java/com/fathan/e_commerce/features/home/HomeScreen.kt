package com.fathan.e_commerce.features.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.features.components.BottomNavigationBar
import com.fathan.e_commerce.features.components.BottomTab
import com.fathan.e_commerce.features.home.ui.components.ShimmerProductGridRows
import com.fathan.e_commerce.features.home.ui.components.ShimmerTabsRow
import com.fathan.e_commerce.features.theme.BackgroundGray
import com.fathan.e_commerce.features.theme.GreenPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onHomeClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onPromoClick: () -> Unit,
) {
    val products by homeViewModel.products.collectAsState()
    val categories by homeViewModel.categories.collectAsState()
    val flashDeals by homeViewModel.flashDeals.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = BackgroundGray,
        modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomTab.HOME,
                onHomeClick = onHomeClick,
                onProfileClick = onProfileClick,
                onChatClick = onChatClick,
                onPromoClick = onPromoClick,
                onTransactionClick = onTransactionClick
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Top Bar with Search
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Search Bar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onSearchClick() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔍",
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Cari di sini...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        IconWithBadge("💬", onClick = onChatClick)
                        IconWithBadge("🛒", onClick = onCartClick)
                    }
                }
            }

            // Banner Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00C853),
                                    Color(0xFF00ACC1)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Promo Guncang 12.12",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Flash Sale s.d. ",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "80%",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFEB3B)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Dynamic Tabs Section (sticky)
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    if (isLoading && categories.isEmpty()) {
                        ShimmerTabsRow(count = 6)
                    } else {
                        CategoryTabsRow(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { homeViewModel.refresh(it) }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            // Loading -> shimmer, else show content
            if (isLoading) {
                item { ShimmerProductGridRows(rows = 4) }
            } else if (selectedCategory == null) {
                // Show Flash Deals (FlashSaleWithProduct)
                items(flashDeals.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { flashDeal ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = flashDeal.product,
                                    flashInfo = flashDeal.flash,
                                    onClick = { onProductClick(flashDeal.product.id) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (flashDeals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada promo saat ini", color = Color.Gray)
                        }
                    }
                }
            } else {
                // Show Filtered Products (Product)
                items(products.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = product,
                                    flashInfo = null,
                                    onClick = { onProductClick(product.id) }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (products.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Tidak ada produk di kategori ini", color = Color.Gray)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ============================================
// CATEGORY TABS COMPONENTS
// ============================================

@Composable
fun CategoryTabsRow(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            DynamicTab(
                label = "For You",
                icon = "✨",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }

        items(categories) { category ->
            DynamicTab(
                label = category.name,
                icon = category.iconEmoji,
                isSelected = selectedCategory?.id == category.id,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun DynamicTab(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) GreenPrimary else Color(0xFFF5F5F5),
        animationSpec = tween(300),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF757575),
        animationSpec = tween(300),
        label = "tabContent"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier
            .height(36.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

// ============================================
// PRODUCT CARD (unchanged)
// ============================================

@Composable
fun ProductCard(
    product: com.fathan.e_commerce.domain.entities.product.Product,
    flashInfo: com.fathan.e_commerce.domain.entities.product.FlashSaleItem? = null,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1).uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.Gray
                )

                if (flashInfo != null && flashInfo.stock > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color(0xFFE53935),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${flashInfo.stock} left",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = Color(0xCC000000),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Mall",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    minLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                val displayPrice = flashInfo?.flashPrice ?: product.price
                Text(
                    text = "Rp$displayPrice",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                )

                if (flashInfo != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rp${product.price}",
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        val discount = try {
                            ((product.price - flashInfo.flashPrice) / product.price * 100).toInt()
                        } catch (e: Exception) { 0 }
                        if (discount > 0) {
                            Text(text = "$discount%", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐ ${product.rating}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = " • ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "Sold ${flashInfo?.sold ?: product.storeId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

// ============================================
// UTIL
// ============================================

@Composable
private fun IconWithBadge(iconText: String, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = iconText, fontSize = 24.sp)
    }
}
