package com.fathan.e_commerce.features.product.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fathan.e_commerce.data.models.RecommendedDto

@Composable
fun RecommendedSection(
    list: List<RecommendedDto>,
    onOpenProduct: (Int) -> Unit
) {
    Column(Modifier.padding(start = 16.dp)) {
        Text("Rekomendasi Untukmu", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) { p ->
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { onOpenProduct(p.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = p.thumbnail,
                            contentDescription = null,
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        Column(Modifier.padding(10.dp)) {
                            Text(p.name, maxLines = 2, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Rp ${formatCurrency(p.price)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))
    }
}
