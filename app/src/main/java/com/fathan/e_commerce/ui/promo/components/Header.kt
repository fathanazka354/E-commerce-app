package com.fathan.e_commerce.ui.promo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.data.models.PromoProduct
import com.fathan.e_commerce.ui.promo.MelokalBatikCard
import com.fathan.e_commerce.ui.promo.TokoGreen
import com.fathan.e_commerce.ui.promo.VoucherCard

@Composable
fun PromoHeaderSection(
    onLocalProductClick: () -> Unit,
    onFlashSaleClick: () -> Unit,
    melokalProducts: List<PromoProduct>
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(TokoGreen)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Bangga Produk Lokal",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Belanja produk pilihan karya anak bangsa",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionTitlePill(text = "Voucher Spesial Beli Lokal")
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(3) { index ->
                VoucherSection(
                    title = "Diskon 8% s.d 10rb",
                    minPurchase = "Min. belanja Rp80rb",
                    remainingText = "00 : 00 : 00",
                    statusText = if (index == 0) "Periode Berakhir" else "Segera Habis"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitlePill(text = "Toko Lokal Pilihan")
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(6) {
                LocalShopCard(
                    name = "KAHF_NEW",
                    subtitle = "Cek Sekarang!"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitlePill(text = "Voucher Spesial Beli Lokal")
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(3) {
                VoucherSection(
                    title = "Diskon 8% s.d 10rb",
                    minPurchase = "Min. belanja Rp80rb",
                    remainingText = "00 : 00 : 00",
                    statusText = "Periode Berakhir"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitlePill(text = "Melokal Dengan Batik", onClick = onLocalProductClick)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(melokalProducts.take(10)) { product ->
                MelokalBatikCard(
                    product = product,
                    modifier = Modifier.width(180.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}



@Composable
fun LocalShopCard(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(110.dp)
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo / avatar toko
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE0E0E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
