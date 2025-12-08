package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProductDetailShimmer() {
    Column(Modifier.padding(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .shimmerEffect()
        )

        Spacer(Modifier.height(16.dp))

        repeat(6) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(vertical = 6.dp)
                    .shimmerEffect()
            )
        }
    }


}
    fun Modifier.shimmerEffect(): Modifier = this
        .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
