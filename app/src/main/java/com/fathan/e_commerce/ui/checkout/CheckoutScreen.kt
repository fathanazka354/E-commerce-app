package com.fathan.e_commerce.ui.checkout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.domain.model.CartItem
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import com.fathan.e_commerce.ui.theme.CardWhite
import com.fathan.e_commerce.ui.theme.TextSecondary

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onBack: () -> Unit
) {
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val shipping = if (cartItems.isEmpty()) 0.0 else 15.0
    val total = subtotal + shipping

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 10.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { /* place order */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Checkout")
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "←",
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(end = 8.dp),
                    fontSize = 20.sp
                )
                Text("Cart", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item)
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    if (cartItems.isEmpty()) {
                        Text(
                            "Your cart is empty.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("Enter Promo Code", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RowItem("Sub Total", subtotal)
                        RowItem("Shipping & Tax", shipping)
                        RowItem("Total", total, bold = true)
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun CartItemCard(item: CartItem) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(BlueSoftBackground, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.product.name.split(" ").first(), fontSize = 11.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(item.product.brand, fontSize = 12.sp, color = TextSecondary)
                    if (item.selectedColor != null || item.selectedStorage != null) {
                        Text(
                            "Color: ${item.selectedColor ?: "-"} • Storage: ${item.selectedStorage ?: "-"}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("$${item.product.price}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Text("x${item.quantity}")
            }
        }
    }
}

@Composable
private fun RowItem(label: String, value: Double, bold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(
            "$${String.format("%.2f", value)}",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}