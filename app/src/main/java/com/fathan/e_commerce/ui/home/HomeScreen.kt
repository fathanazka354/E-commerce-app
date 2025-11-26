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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.domain.model.Category
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.ui.theme.BluePrimary
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    categories: List<Category>,
    onHomeClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistClick: () -> Unit
) {
    val products by homeViewModel.products.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomTab.HOME,
                onHomeClick = onHomeClick,
                onCartClick = onCartClick,
                onProfileClick = onProfileClick,
                onChatClick = onChatClick,
                onWishlistClick = onWishlistClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BlueSoftBackground)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)) }

            /* Search + icons row */
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = CardWhite,
//                        tonalElevation = 2.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    Log.i("HomeScreen", "Search clicked")
                                    onSearchClick()
                                }
                        ) {
                            TextField(
                                value = "",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text("Search") },
                                singleLine = true,
                                modifier = Modifier
                                    .height(54.dp)
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = CardWhite,
                                    focusedIndicatorColor = CardWhite,
                                    unfocusedIndicatorColor = CardWhite
                                )
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

            /* Banner */
            item {
                Surface(
                    color = BluePrimary,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "iPhone 16 Pro",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "Extraordinary Visual &\nExceptional Power",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Button(
                            onClick = { /* maybe open the first product */ },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Shop Now")
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            /* Categories */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("See All", fontSize = 13.sp, color = BluePrimary)
                }
                Spacer(Modifier.height(12.dp))
            }

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

            /* Products list */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Flash Deals for You", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("See All", fontSize = 13.sp, color = BluePrimary)
                }
                Spacer(Modifier.height(12.dp))
            }

            items(products) { product ->
                ProductCard(product = product, onClick = { onProductClick(product) })
                Spacer(Modifier.height(12.dp))
            }

            item { Spacer(Modifier.height(72.dp)) } // bottom padding for nav
        }
    }
}

@Composable
private fun SmallCircleIcon(
    iconText: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(CardWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(iconText)
    }
}

@Composable
fun CategoryCard(category: Category) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .size(width = 96.dp, height = 96.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(category.iconEmoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(category.name, fontSize = 13.sp)
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BlueSoftBackground),
                contentAlignment = Alignment.Center
            ) {
                // just show abbreviation of name
                Text(text = product.name.split(" ").first(), fontSize = 11.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(product.brand, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text("$${product.price}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("⭐ ${product.rating}", fontSize = 12.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: BottomTab, onHomeClick: () -> Unit, onCartClick: () -> Unit, onProfileClick: () -> Unit = {}, onChatClick: () -> Unit = {}, onWishlistClick: () -> Unit) {
    Surface(tonalElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("🏠", "Home", selected = selectedTab == BottomTab.HOME, onClick = onHomeClick)
            BottomItem("💬", "Chat", selected = selectedTab == BottomTab.CHAT, onClick = onChatClick)
            BottomItem("❤️", "Wishlist", selected = selectedTab == BottomTab.WISHLIST, onClick = onWishlistClick)
            BottomItem("🛒", "Cart", selected = selectedTab == BottomTab.CART, onClick = onCartClick)
            BottomItem("👤", "Profile", selected = selectedTab == BottomTab.PROFILE, onClick = onProfileClick)
        }
    }
}

@Composable
private fun BottomItem(icon: String, label: String,
                       selected: Boolean, onClick: () -> Unit = {}) {
    val bgColor = if (selected) BlueSoftBackground else Color.Transparent
    val textColor = if (selected) BluePrimary else TextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = textColor)
        Text(label, fontSize = 11.sp, color = textColor)
    }
}