package com.fathan.e_commerce.features.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.fathan.e_commerce.features.components.BottomTab
import com.fathan.e_commerce.features.components.ECommerceTopBar
import com.fathan.e_commerce.features.components.BottomNavigationBar
import java.text.NumberFormat
import java.util.Locale

val TokoGreen = Color(0xFF03AC0E)

enum class FilterType {
    STATUS, PRODUCT, DATE
}

data class TransactionItem(
    val id: String,
    val date: String,
    val status: String,
    val type: String = "Belanja",
    val productName: String,
    val productCount: Int,
    val productImageUrl: String,
    val totalPrice: Double,
    val isDiscount: Boolean = false,
    val discountAmount: Double = 0.0
)

val dummyTransactions = listOf(
    TransactionItem("1", "19 Sep 2025", "Selesai", "Belanja", "KZ ZS10 Pro X Metal Earphone with Mic...", 1, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500", 754941.0),
    TransactionItem("2", "11 Sep 2025", "Selesai", "Belanja", "NTag215 PVC Coin 25mm RFID NFC Pr...", 5, "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500", 16550.0, true, 162.0),
    TransactionItem("3", "17 Jul 2025", "Selesai", "Belanja", "AONIJIE SD21 Soft Flask Water Bottle...", 2, "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500", 197253.0)
)

val statusOptions = listOf(
    "Semua Status Transaksi", "Semua Transaksi Berlangsung", "Menunggu Konfirmasi",
    "Diproses", "Dikirim", "Tiba di Tujuan", "Dikomplain", "Berhasil", "Tidak Berhasil"
)

val productOptions = listOf(
    "Semua Produk", "Belanja", "Top-up & Tagihan", "Travel & Entertainment",
    "Keuangan", "PLUS", "Yep NOW!", "GoFood", "Lainnya"
)

val dateOptions = listOf(
    "Semua Tanggal Transaksi", "30 Hari Terakhir", "90 Hari Terakhir", "Pilih Tanggal Sendiri"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Filter
    var activeFilterType by remember { mutableStateOf<FilterType?>(null) }
    var selectedStatus by remember { mutableStateOf(statusOptions[0]) }
    var selectedProduct by remember { mutableStateOf(productOptions[0]) }
    var selectedDate by remember { mutableStateOf(dateOptions[0]) }

    // Helper untuk mengubah text Chip agar tidak kepanjangan
    fun getChipLabel(type: FilterType): String {
        return when (type) {
            FilterType.STATUS -> if (selectedStatus == statusOptions[0]) "Semua Status" else selectedStatus
            FilterType.PRODUCT -> if (selectedProduct == productOptions[0]) "Semua Produk" else selectedProduct
            FilterType.DATE -> if (selectedDate == dateOptions[0]) "Semua Tanggal" else selectedDate
        }
    }

    Scaffold(
        topBar = {
            ECommerceTopBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari Transaksi",
                onCartClick = onCartClick,
                onChatClick = onChatClick,
                onBackClick = onHomeClick
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                selectedTab = BottomTab.TRANSACTION, // Tab Transaksi aktif
                onProfileClick = onProfileClick,
                onChatClick = onChatClick
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipButton(
                        label = getChipLabel(FilterType.STATUS),
                        onClick = { activeFilterType = FilterType.STATUS }
                    )
                }
                item {
                    FilterChipButton(
                        label = getChipLabel(FilterType.PRODUCT),
                        onClick = { activeFilterType = FilterType.PRODUCT }
                    )
                }
                item {
                    FilterChipButton(
                        label = getChipLabel(FilterType.DATE),
                        onClick = { activeFilterType = FilterType.DATE }
                    )
                }
            }

            Divider(thickness = 1.dp, color = Color(0xFFF0F0F0))

            // Transaction List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dummyTransactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }

        // --- Modal Bottom Sheet ---
        if (activeFilterType != null) {
            ModalBottomSheet(
                onDismissRequest = { activeFilterType = null },
                containerColor = Color.White,
                dragHandle = null // Custom header handle
            ) {
                val (title, options, currentSelection, onSelect) = when (activeFilterType) {
                    FilterType.STATUS -> Quadruple(
                        "Mau lihat status apa?", statusOptions, selectedStatus
                    ) { s: String -> selectedStatus = s }
                    FilterType.PRODUCT -> Quadruple(
                        "Mau lihat produk apa?", productOptions, selectedProduct
                    ) { s: String -> selectedProduct = s }
                    FilterType.DATE -> Quadruple(
                        "Pilih tanggal", dateOptions, selectedDate
                    ) { s: String -> selectedDate = s }
                    else -> Quadruple("", emptyList(), "") { _: String -> }
                }

                FilterBottomSheetContent(
                    title = title,
                    options = options,
                    selectedOption = currentSelection,
                    onOptionSelected = onSelect,
                    onDismiss = { activeFilterType = null },
                    isDateFilter = activeFilterType == FilterType.DATE // Special case for Date (Button Terapkan)
                )
            }
        }
    }
}


@Composable
fun FilterChipButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        color = Color(0xFFF5F5F5),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.width(4.dp))
            Text("⌄", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDateFilter: Boolean
) {
    var tempSelected by remember { mutableStateOf(selectedOption) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                "Close",
                modifier = Modifier.clickable { onDismiss() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color(0xFFF0F0F0))

        // List Options
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false) // Wrap content height but constrained
                .heightIn(max = 400.dp) // Max height for list
        ) {
            items(options) { option ->
                val isSelected = if (isDateFilter) option == tempSelected else option == selectedOption

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isDateFilter) {
                                tempSelected = option
                            } else {
                                onOptionSelected(option)
                                onDismiss()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        option,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        color = Color.Black
                    )

                    // Radio Button Style
                    RadioButton(
                        selected = isSelected,
                        onClick = null, // Handled by Row click
                        colors = RadioButtonDefaults.colors(selectedColor = TokoGreen)
                    )
                }
                Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Button Terapkan (Only for Date Filter)
        if (isDateFilter) {
            PaddingValues(16.dp).let { padding ->
                Button(
                    onClick = {
                        onOptionSelected(tempSelected)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TokoGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                ) {
                    Text("Terapkan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionItem) {
    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Card
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, null, tint = TokoGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(transaction.type, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(transaction.date, color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        transaction.status,
                        color = TokoGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

            // Product Info
            Row(verticalAlignment = Alignment.Top) {
                Image(
                    painter = rememberAsyncImagePainter(transaction.productImageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        transaction.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${transaction.productCount} barang", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Price
            Column {
                Text("Total Belanja:", fontSize = 12.sp, color = Color.Gray)
                Text(
                    formatRupiah(transaction.totalPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (transaction.isDiscount) {
                    Text(
                        "Diskon ${formatRupiah(transaction.discountAmount)}",
                        color = Color(0xFFD6001C),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TokoGreen),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Ulas", color = TokoGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TokoGreen),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Beli Lagi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp").replace(",00", "")
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
