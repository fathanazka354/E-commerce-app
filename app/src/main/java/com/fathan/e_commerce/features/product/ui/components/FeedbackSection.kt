package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackSection(
    avg: Double,
    total: Int,
    onClick: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Penilaian & Ulasan", fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Lihat Semua >", modifier = Modifier.clickable(onClick = onClick))
        }

        Spacer(Modifier.height(6.dp))
        Text("⭐ $avg dari $total ulasan", fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
    }
}
