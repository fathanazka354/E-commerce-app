package com.fathan.e_commerce.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.domain.entities.product.Category
import com.fathan.e_commerce.domain.entities.product.FlashSaleWithProduct
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.ui.theme.BluePrimary
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onHomeClick: () -> Unit,
    onProductClick: (FlashSaleWithProduct) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onPromoClick: () -> Unit,
) {
    val products by homeViewModel.products.collectAsState()
    val categories by homeViewModel.categories.collectAsState()

    val promoItems by homeViewModel.flashDeals.collectAsState()


    Scaffold(
        containerColor = BlueSoftBackground,
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BlueSoftBackground),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() ,
                bottom = paddingValues.calculateBottomPadding() + 24.dp // Extra 24dp untuk safety
            )
        ) {

            // Search + Icons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CardWhite,
                        modifier = Modifier.weight(1f),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    Log.i("HomeScreen", "Search clicked")
                                    onSearchClick()
                                }
                                .height(54.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Search",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))
                    SmallCircleIcon("🔔")
                    Spacer(Modifier.width(8.dp))
                    SmallCircleIcon("🛒", onClick = onCartClick)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Featured Banner
            item {
                Surface(
                    color = BluePrimary,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "iPhone 16 Pro",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Extraordinary Visual &\nExceptional Power",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Button(
                            onClick = { /* Open product details */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B4513)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Shop Now",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Categories Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    TextButton(onClick = { /* Navigate to all categories */ }) {
                        Text(
                            text = "See All",
                            fontSize = 14.sp,
                            color = BluePrimary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Categories Horizontal List
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(category)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Flash Deals Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flash Deals for You",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    TextButton(onClick = { /* Navigate to all products */ }) {
                        Text(
                            text = "See All",
                            fontSize = 14.sp,
                            color = BluePrimary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Products List
            items(promoItems) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Extra bottom spacing untuk last item
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SmallCircleIcon(
    iconText: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = CardWhite,
        shadowElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = iconText,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun CategoryCard(category: Category) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        modifier = Modifier
            .size(width = 100.dp, height = 110.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = category.iconEmoji,
                fontSize = 48.dp.value.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProductCard(product: FlashSaleWithProduct, onClick: () -> Unit) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlueSoftBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.product.name.take(1),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = product.product.brand,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$${product.product.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐",
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = product.product.rating.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}