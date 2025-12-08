package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.features.product.ui.ProductDetailUiState

@Composable
fun DescriptionSection(ui: ProductDetailUiState) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text("Deskripsi", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(ui.product.description ?: "-", fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
    }
}
