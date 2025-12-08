package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.features.product.ui.ProductDetailUiState
import com.fathan.e_commerce.features.theme.GreenPrimary
import com.fathan.e_commerce.features.theme.TextSecondary
import java.text.NumberFormat

@Composable
fun ProductHeaderSection(ui: ProductDetailUiState) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(ui.product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐ ${ui.avgRating}", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text("(${ui.reviewCount} ulasan)", fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text("${ui.product.sold} terjual", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(Modifier.height(10.dp))

        if (ui.flashSale != null) {
            FlashPriceSection(ui)
        } else {
            Text(
                text = "Rp ${formatCurrency(ui.product.price.toLong())}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
        }
    }
}


fun formatCurrency(value: Long): String {
    // simple thousand separator
    return NumberFormat.getInstance().format(value)
}
