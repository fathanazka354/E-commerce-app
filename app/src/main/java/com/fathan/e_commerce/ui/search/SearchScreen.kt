package com.fathan.e_commerce.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.fathan.e_commerce.domain.entities.product.Product
import com.fathan.e_commerce.ui.theme.BluePrimary
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val query by searchViewModel.query.collectAsState()
    val results by searchViewModel.result.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoftBackground)
            .padding(16.dp)
    ) {

        // Top AppBar Search
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "←",
                fontSize = 28.sp,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onBack() }
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = query,
                    onValueChange = { newText ->
                        // update query in ViewModel, debounce can be added inside VM if needed
                        searchViewModel.updateQuery(newText)
                    },
                    placeholder = { Text("Search products...") },
                    singleLine = true,
                    modifier = Modifier
                        .height(52.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = CardWhite,
                        focusedIndicatorColor = CardWhite,
                        unfocusedIndicatorColor = CardWhite
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // When no text typed
        if (query.isBlank()) {
            Text("Recent Searches", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            RecentSearchItem(text = "iPhone 16") {
                // quick search when clicked
                scope.launch { searchViewModel.updateQuery("iPhone 16") }
            }
            RecentSearchItem(text = "Headphones") {
                scope.launch { searchViewModel.updateQuery("Headphones") }
            }
            RecentSearchItem(text = "Samsung S24 Ultra") {
                scope.launch { searchViewModel.updateQuery("Samsung S24 Ultra") }
            }

            Spacer(Modifier.height(32.dp))

            Text("Popular Searches", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            PopularChip("iPhone") { scope.launch { searchViewModel.updateQuery("iPhone") } }
            PopularChip("Smartwatch") { scope.launch { searchViewModel.updateQuery("Smartwatch") } }
            PopularChip("Laptop") { scope.launch { searchViewModel.updateQuery("Laptop") } }
            PopularChip("Headphones") { scope.launch { searchViewModel.updateQuery("Headphones") } }

        } else {
            // Show filtered results coming from ViewModel -> UseCase -> Repository -> Remote
            Text(
                "Results (${results.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn {
                items(results) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RecentSearchItem(text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun PopularChip(text: String, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(end = 8.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}


@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
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
                    text = product.name.take(1),
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
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = product.brand,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$${product.price}",
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
                    text = product.rating.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}