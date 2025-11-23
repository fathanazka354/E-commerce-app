package com.fathan.e_commerce.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.models.DummyData
import com.fathan.e_commerce.models.Product
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        if (query.isBlank()) emptyList()
        else DummyData.products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.brand.contains(query, ignoreCase = true)
        }
    }

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
                    onValueChange = { query = it },
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

            RecentSearchItem("iPhone 16")
            RecentSearchItem("Headphones")
            RecentSearchItem("Samsung S24 Ultra")

            Spacer(Modifier.height(32.dp))

            Text("Popular Searches", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            PopularChip("iPhone")
            PopularChip("Smartwatch")
            PopularChip("Laptop")
            PopularChip("Headphones")

        } else {
            // Show filtered results
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
fun RecentSearchItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* do search */ }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun PopularChip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(end = 8.dp)
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