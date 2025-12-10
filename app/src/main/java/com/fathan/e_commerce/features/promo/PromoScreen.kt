package com.fathan.e_commerce.features.promo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.fathan.e_commerce.features.product.data.model.PromoProduct
import com.fathan.e_commerce.features.components.BottomTab
import com.fathan.e_commerce.features.components.ECommerceTopBar
import com.fathan.e_commerce.features.components.BottomNavigationBar
import com.fathan.e_commerce.features.promo.components.PromoCategoryTabs
import com.fathan.e_commerce.features.promo.components.PromoHeaderSection
import com.fathan.e_commerce.features.promo.components.PromoLabelChip
import com.fathan.e_commerce.features.promo.components.PromoProductCard
import com.fathan.e_commerce.features.wishlist.formatRupiah

val TokoGreen = Color(0xFF03AC0E)
val TokoRed = Color(0xFFD6001C)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromoScreen(
    onHomeClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLocalProductClick: () -> Unit,
    onFlashSaleClick: () -> Unit,
    viewModel: PromoViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val promoProducts by viewModel.promoProducts.collectAsState()

    val displayedProducts = remember(promoProducts, selectedCategory) {
        viewModel.getFilteredPromoProducts()
    }

    Scaffold(
        topBar = {
            ECommerceTopBar(
                query = "",
                onQueryChange = {},
                placeholder = "Cari di Sini",
                onCartClick = onCartClick,
                onChatClick = {}
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                selectedTab = BottomTab.PROMO,
                onProfileClick = onProfileClick,
                onChatClick = onCartClick,
                onTransactionClick = onTransactionClick
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        PromoContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            categories = viewModel.categories,
            selectedCategory = selectedCategory,
            onCategorySelected = viewModel::setCategory,
            products = displayedProducts,
            onLocalProductClick = onLocalProductClick,
            onFlashSaleClick = onFlashSaleClick
        )
    }
}

@ExperimentalFoundationApi
@Composable
private fun PromoContent(
    modifier: Modifier = Modifier,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    products: List<PromoProduct>,
    onLocalProductClick: () -> Unit,
    onFlashSaleClick: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            bottom = 16.dp,
            top = 0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PromoHeaderSection(
                onLocalProductClick = onLocalProductClick,
                onFlashSaleClick = onFlashSaleClick,
                melokalProducts = products
            )
        }

        stickyHeader {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
            ) {
                PromoCategoryTabs(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }
        }

        promoProductGridItems(products)
    }
}

@Composable
fun MelokalBatikCard(
    product: PromoProduct,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(270.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )

                if (product.discountPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(
                                color = TokoRed,
                                shape = RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 8.dp,
                                    bottomStart = 0.dp
                                )
                            )
                    ) {
                        Text(
                            text = ">${product.discountPercentage}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PromoLabelChip(text = "Gajian Sale")
                    PromoLabelChip(text = "PLUS", background = Color(0xFF008F5A))
                    PromoLabelChip(text = "Bonus", background = Color(0xFFE91E63))
                }
            }

            // ==== CONTENT ====
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // chip harga
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFEEF0),
                                shape = RoundedCornerShape(999.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCard,
                                contentDescription = null,
                                tint = Color(0xFFE5394F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatRupiah(product.price),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFE5394F)
                            )
                        }
                    }

                    if (product.originalPrice > product.price) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatRupiah(product.originalPrice),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = " ${product.rating} • ${product.soldCount} terjual",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF9C27B0), shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dowa Bag",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


private fun LazyListScope.promoProductGridItems(
    products: List<PromoProduct>
) {
    val rows = if (products.isEmpty()) 0 else (products.size + 1) / 2

    items(rows) { rowIndex ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val firstIndex = rowIndex * 2
            val secondIndex = firstIndex + 1

            PromoProductCard(
                product = products[firstIndex],
                modifier = Modifier.weight(1f)
            )

            if (secondIndex < products.size) {
                PromoProductCard(
                    product = products[secondIndex],
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}