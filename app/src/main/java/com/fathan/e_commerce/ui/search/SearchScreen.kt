package com.fathan.e_commerce.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.domain.model.DummyData
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.ui.home.ProductCard
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
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
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(BlueSoftBackground)
            .padding(16.dp)
    ) {

        // Top AppBar Search
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                "←",
                fontSize = 28.sp,
                modifier = Modifier.Companion
                    .padding(end = 16.dp)
                    .clickable { onBack() }
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardWhite,
                modifier = Modifier.Companion.weight(1f)
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search products...") },
                    singleLine = true,
                    modifier = Modifier.Companion
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

        Spacer(Modifier.Companion.height(24.dp))

        // When no text typed
        if (query.isBlank()) {
            Text("Recent Searches", fontWeight = FontWeight.Companion.Bold, fontSize = 18.sp)
            Spacer(Modifier.Companion.height(12.dp))

            RecentSearchItem("iPhone 16")
            RecentSearchItem("Headphones")
            RecentSearchItem("Samsung S24 Ultra")

            Spacer(Modifier.Companion.height(32.dp))

            Text("Popular Searches", fontWeight = FontWeight.Companion.Bold, fontSize = 18.sp)
            Spacer(Modifier.Companion.height(12.dp))

            PopularChip("iPhone")
            PopularChip("Smartwatch")
            PopularChip("Laptop")
            PopularChip("Headphones")

        } else {
            // Show filtered results
            Text(
                "Results (${results.size})",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 18.sp,
                modifier = Modifier.Companion.padding(bottom = 12.dp)
            )

            LazyColumn {
                items(results) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                    Spacer(Modifier.Companion.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RecentSearchItem(text: String) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable { /* do search */ }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun PopularChip(text: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = CardWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.Companion
            .padding(bottom = 8.dp)
            .padding(end = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.Companion
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}