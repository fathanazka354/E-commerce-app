package com.fathan.e_commerce.ui.product
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@Composable
fun ProductDetailScreen(
    productDetailViewModel: ProductDetailViewModel,
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (String?, String?) -> Unit
) {
    val isFavorite by productDetailViewModel.isFavorite(product.id).collectAsState(initial = false)

    val quantity by productDetailViewModel.quantity.collectAsState(initial = 0)
    var selectedColor by remember { mutableStateOf(product.colors.firstOrNull()) }
    var selectedStorage by remember { mutableStateOf(product.storages.firstOrNull()) }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 10.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Buy now flow */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Buy Now")
                    }
                    Button(
                        onClick = { onAddToCart(selectedColor, selectedStorage) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Add to Cart")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlueSoftBackground)
                .padding(innerPadding)
        ) {

            // top image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(CardWhite),
            ) {
                // fake navigation back button
                Text(
                    text = "←",
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable { onBack() },
                    fontSize = 20.sp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(160.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(BlueSoftBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(product.name.split(" ").first(), fontSize = 14.sp)
                    IconButton(onClick = { productDetailViewModel.toggle(product.id) }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("By ${product.brand}", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("⭐ ${product.rating}  (${product.ratingCount} reviews)", fontSize = 13.sp)

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    product.oldPrice?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$$it",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    QuantitySelector(
                        quantity = quantity,
                        onIncrease = { productDetailViewModel.increaseQuantity() },
                        onDecrease = { productDetailViewModel.decreaseQuantity() }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (product.colors.isNotEmpty()) {
                    Text("Color", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.colors.size) { idx ->
                            val color = product.colors[idx]
                            FilterChip(
                                selected = color == selectedColor,
                                onClick = { selectedColor = color },
                                label = { Text(color, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                if (product.storages.isNotEmpty()) {
                    Text("Storage", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(product.storages.size) { idx ->
                            val storage = product.storages[idx]
                            FilterChip(
                                selected = storage == selectedStorage,
                                onClick = { selectedStorage = storage },
                                label = { Text(storage, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text("A Snapshot View", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoundIconButton(text = "-", onClick = onDecrease)
        Text(quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        RoundIconButton(text = "+", onClick = onIncrease)
    }
}

@Composable
private fun RoundIconButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(CardWhite)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}