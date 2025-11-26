package com.fathan.e_commerce.ui.wishlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.fathan.e_commerce.R
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.ui.home.BottomNavigationBar

// --- Dummy Data Model ---
data class WishlistProduct(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String, // Using URL for Coil
    val category: String,
    val isFeatured: Boolean = false
)

// --- Dummy Data ---
val allProducts = listOf(
    WishlistProduct(1, "Canceling Headphone", 149.99, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500", "Electronics", false),
    WishlistProduct(2, "Leather Handbag", 49.99, "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500", "Fashion", false),
    WishlistProduct(3, "Smart Watch Series 7", 299.00, "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500", "Electronics", false),
    WishlistProduct(4, "Running Shoes", 89.50, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500", "Fashion", false),
    // Featured
    WishlistProduct(5, "Memory Foam Pillow", 39.99, "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=500", "Home", true),
    WishlistProduct(6, "Vitamin C Serum", 24.99, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500", "Beauty", true),
    WishlistProduct(7, "Minimalist Lamp", 55.00, "https://images.unsplash.com/photo-1507473888900-52e1ad98ca2a?w=500", "Home", true)
)

enum class SortOption {
    DEFAULT, PRICE_HIGH_LOW, PRICE_LOW_HIGH, NAME_A_Z
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWishlistClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(SortOption.DEFAULT) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Electronics", "Fashion", "Home", "Beauty")

    // --- Filtering Logic ---
    val filteredProducts = remember(searchQuery, selectedSort, selectedCategory) {
        var list = allProducts

        // 1. Search
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        // 2. Category
        if (selectedCategory != "All") {
            list = list.filter { it.category == selectedCategory }
        }

        // 3. Sorting
        list = when (selectedSort) {
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOption.NAME_A_Z -> list.sortedBy { it.name }
            SortOption.DEFAULT -> list
        }
        list
    }

    val bestProducts = filteredProducts.filter { !it.isFeatured }
    val featuredProducts = filteredProducts.filter { it.isFeatured }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                selectedTab = BottomTab.WISHLIST, // Highlight Wishlist tab
                onProfileClick = onProfileClick,
                onCartClick = onCartClick,
                onWishlistClick = onWishlistClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // --- Header ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp).clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Shop", // Or "Wishlist" per your request
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Search & Filter ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Search Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Search, "Search", tint = Color.LightGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search..", color = Color.LightGray)
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Filter Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { showFilterSheet = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, "Filter", tint = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Grid Content ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Best Products Header
                if (bestProducts.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader("Best Products")
                    }
                    items(bestProducts) { product ->
                        ProductCard(product)
                    }
                }

                // 2. Featured Products Header
                if (featuredProducts.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader("Featured Product")
                    }
                    items(featuredProducts) { product ->
                        ProductCard(product)
                    }
                }
            }
        }
    }

    // --- Filter Bottom Sheet ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Sort & Filter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Sort By Price", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedSort == SortOption.PRICE_HIGH_LOW,
                        onClick = { selectedSort = SortOption.PRICE_HIGH_LOW },
                        label = { Text("Highest Price") }
                    )
                    FilterChip(
                        selected = selectedSort == SortOption.PRICE_LOW_HIGH,
                        onClick = { selectedSort = SortOption.PRICE_LOW_HIGH },
                        label = { Text("Lowest Price") }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835))
                ) {
                    Text("Apply Filters", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            "See All",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun ProductCard(product: WishlistProduct) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp, // Flat look like image
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Image & Heart Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)) // Light gray bg for image
            ) {
                // Product Image
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )

                // Heart Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price & Add Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PunchClock, // Ensure you have a clock icon or use Icons.Default.Schedule
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "27.12-05.02",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray, fontSize = 10.sp)
                        )
                    }
                }

                // Yellow Add Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDD835)) // Yellow
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
