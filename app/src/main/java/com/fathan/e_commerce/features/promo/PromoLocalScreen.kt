package com.fathan.e_commerce.features.promo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.features.product.data.model.PromoVoucher
import com.fathan.e_commerce.features.promo.components.PromoProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoLocalScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: PromoViewModel = hiltViewModel()
) {
    val vouchers by viewModel.vouchers.collectAsState()
    val localProducts by viewModel.localProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cari di Sini", fontSize = 14.sp, color = Color.Gray) }, // Simplified search bar look
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) { Icon(Icons.Outlined.ShoppingCart, "Cart", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TokoGreen, titleContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header Section ---
            item(span = { GridItemSpan(2) }) {
                Column {
                    // Green Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TokoGreen)
                            .padding(24.dp)
                    ) {
                        Column {
                            Text("Bangga Produk Lokal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Dukung UMKM Indonesia", fontSize = 14.sp, color = Color.White)
                        }
                    }

                    // Voucher Section
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Voucher Spesial Beli Lokal", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(vouchers) { voucher ->
                            VoucherCard(voucher)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Section Title
                    Text("Melokal Dengan Batik", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- Product List ---
            items(localProducts) { product ->
                PromoProductCard(product)
            }
        }
    }
}

@Composable
fun VoucherCard(voucher: PromoVoucher) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)), // Pinkish bg
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC1C1)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(280.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(voucher.title, fontWeight = FontWeight.Bold, color = TokoRed, fontSize = 14.sp)
                Text(voucher.subtitle, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Berakhir: ${voucher.endDate}", fontSize = 10.sp, color = Color.Gray)
            }
            // Separator line could be added here
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Diskon", fontSize = 10.sp, color = Color.Gray)
                Text(voucher.discountText, fontWeight = FontWeight.Bold, color = TokoRed)
            }
        }
    }
}
