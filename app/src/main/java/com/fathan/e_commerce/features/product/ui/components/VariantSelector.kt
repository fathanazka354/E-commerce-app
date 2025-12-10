package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fathan.e_commerce.features.product.domain.entities.ProductVariant

@Composable
fun VariantSelector(
    variants: List<ProductVariant>,
    selected: Int?,
    onSelect: (Int?) -> Unit
) {
    if (variants.isEmpty()) return

    Column(Modifier.padding(horizontal = 16.dp)) {
        Text("Pilih Varian", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(variants) { variant ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    tonalElevation = if (selected == variant.id) 4.dp else 0.dp,
                    modifier = Modifier.clickable { onSelect(variant.id) }
                ) {
                    Text(
                        variant.name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}
