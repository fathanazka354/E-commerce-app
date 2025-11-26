package com.fathan.e_commerce.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.fathan.e_commerce.domain.model.CartItem
import java.text.NumberFormat
import java.util.Locale

// Warna Hijau a la Tokopedia
val TokoGreen = Color(0xFF03AC0E)
val TokoRed = Color(0xFFD6001C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onBack: () -> Unit,
    onRemoveItem: (CartItem) -> Unit = {},
    onUpdateQuantity: (CartItem, Int) -> Unit = { _, _ -> }
) {
    // Hitung Total
    val totalPrice = cartItems.sumOf { it.product.price * it.quantity }
    val totalItems = cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            Row( modifier = Modifier.padding(16.dp),verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp).clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Keranjang", // Or "Wishlist" per your request
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        bottomBar = {
            BottomCartBar(totalPrice = totalPrice, totalItems = totalItems)
        },
        containerColor = Color(0xFFF5F5F5) // Background abu-abu muda
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
        ) {
            // Header Info
            item {
                InfoBanner()
            }

            if (cartItems.isEmpty()) {
                item {
                    EmptyCartState()
                }
            } else {
                // Group items by "Shop" (Simulasi: Kita anggap semua dari 1 toko "GeekyTech")
                item {
                    CartShopGroup(
                        shopName = "GeekyTech",
                        items = cartItems,
                        onRemoveItem = onRemoveItem,
                        onUpdateQuantity = onUpdateQuantity
                    )
                }
            }

            // Rekomendasi Produk (Sesuai gambar bawah)
            item {
                RecommendationSection()
            }
        }
    }
}

@Composable
fun InfoBanner() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${1} produk terpilih", fontSize = 12.sp, color = Color.Gray)
            Text("Hapus", fontSize = 12.sp, color = TokoGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Banner Hijau
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PLUS", fontWeight = FontWeight.Bold, color = TokoGreen, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voucher diskon 5% khusus buatmu!", fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("Klaim >", fontSize = 12.sp, color = TokoGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartShopGroup(
    shopName: String,
    items: List<CartItem>,
    onRemoveItem: (CartItem) -> Unit,
    onUpdateQuantity: (CartItem, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp), // Kotak full width
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Shop Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomCheckbox(checked = true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(shopName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Surface(color = TokoGreen, shape = RoundedCornerShape(4.dp)) {
                    Text("Official", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List Items
            items.forEach { item ->
                CartItemRow(item, onRemoveItem, onUpdateQuantity)
                if (items.last() != item) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onRemoveItem: (CartItem) -> Unit,
    onUpdateQuantity: (CartItem, Int) -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        CustomCheckbox(checked = true)
        Spacer(modifier = Modifier.width(8.dp))

        // Image
        Image(
            painter = rememberAsyncImagePainter(item.product.thumbnail),
            contentDescription = null,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Variant Chip
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.selectedColor ?: "Default",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Price
            Text(
                text = formatRupiah(item.product.price),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            // Fake Discount UI
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(2.dp)) {
                    Text("12%", fontSize = 10.sp, color = TokoRed, modifier = Modifier.padding(horizontal = 2.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatRupiah(item.product.price * 1.12), // Fake original price
                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough, color = Color.Gray),
                    fontSize = 10.sp
                )
            }
        }
    }

    // Actions (Trash & Counter) - Separate Row below details or aligned right
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 12.dp), // Indent to align with content
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Gray,
            modifier = Modifier.clickable { onRemoveItem(item) }
        )
        Spacer(modifier = Modifier.width(16.dp))

        // Counter
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (item.quantity > 1) onUpdateQuantity(item, item.quantity - 1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = item.quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { onUpdateQuantity(item, item.quantity + 1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = TokoGreen)
                }
            }
        }
    }
}

@Composable
fun BottomCartBar(totalPrice: Double, totalItems: Int) {
    Surface(
        shadowElevation = 16.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Voucher Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F3F1)) // Light green bg
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = TokoGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hemat Rp22rb pakai Gratis Ongkir!", fontSize = 12.sp, color = Color(0xFF2E7D32))
                }
                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier
                    .size(12.dp)
                    .rotate(180f), tint = Color.Gray)
            }

            // Main Bar
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Harga", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        formatRupiah(totalPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = TokoGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(45.dp)
                ) {
                    Text("Beli ($totalItems)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecommendationSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Kamu sempat lihat-lihat ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Dummy Recommendation Card
            RecommendationCard("Insta360 X5", "Rp9.160.520", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500")
            RecommendationCard("Insta360 Ace", "Rp5.636.460", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500")
        }
    }
}

@Composable
fun RecommendationCard(name: String, price: String, imageUrl: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(name, maxLines = 2, fontSize = 12.sp)
                Text(price, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(2.dp)) {
                    Text("Diskon 5%", fontSize = 10.sp, color = TokoGreen, modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyCartState() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(50.dp), contentAlignment = Alignment.Center) {
        Text("Keranjang kamu kosong", color = Color.Gray)
    }
}

@Composable
fun CustomCheckbox(checked: Boolean) {
    Icon(
        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Outlined.Circle,
        contentDescription = null,
        tint = if (checked) TokoGreen else Color.Gray,
        modifier = Modifier.size(24.dp)
    )
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp").replace(",00", "")
}

fun Modifier.rotate(degrees: Float) = this.then(Modifier.graphicsLayer(rotationZ = degrees))
