package com.fathan.e_commerce.features.promo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.features.promo.components.PromoProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoFlashSaleScreen(
    onBack: () -> Unit,
    viewModel: PromoViewModel = hiltViewModel()
) {
    val flashSaleProducts by viewModel.flashSaleProducts.collectAsState()
//    val flashSaleProducts by viewModel.flashSaleProducts.collectAsState()
    val PinkGradient = Brush.verticalGradient(listOf(Color(0xFFE91E63), Color(0xFFC2185B)))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flash Sale", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE91E63))
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Timer Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PinkGradient)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Berakhir Dalam", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    // Dummy Timer
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TimerBox("02")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                        TimerBox("15")
                        Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                        TimerBox("40")
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(flashSaleProducts) { product ->
                    PromoProductCard(product)
                }
            }
        }
    }
}

@Composable
fun TimerBox(time: String) {
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.size(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(time, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
