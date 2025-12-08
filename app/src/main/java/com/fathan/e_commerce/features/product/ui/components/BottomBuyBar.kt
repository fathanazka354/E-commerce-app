package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fathan.e_commerce.features.theme.GreenPrimary

@Composable
fun BottomBuyBar(
    quantity: Int,
    productId: Int,
    variantId: Int?,
    onAddToCart: (Int, Int?, Int) -> Unit,
    onBuy: (Int, Int?, Int) -> Unit
) {
    Surface(shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onBuy(productId, variantId, quantity) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Beli Sekarang")
            }

            Button(
                onClick = { onAddToCart(productId, variantId, quantity) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(GreenPrimary)
            ) {
                Text("+ Keranjang")
            }
        }
    }
}
