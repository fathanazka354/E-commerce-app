package com.fathan.e_commerce.features.wishlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.util.Locale

val TokoRed = Color(0xFFD6001C)

data class ProductWishlistUi(
    val id: Int,
    val name: String,
    val price: Double,
    val originalPrice: Double,
    val discount: Int,
    val imageUrl: String,
    val shopName: String,
    val location: String,
    val rating: Double,
    val soldCount: Int,
    val isOfficial: Boolean = false
)

val dummyWishlistProducts = listOf(
    ProductWishlistUi(1, "GARMIN Vivoactive 5 GPS Smartwatch - Garansi Resmi", 2847110.0, 5600000.0, 47, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500", "Peebee Store", "Jakarta Pusat", 5.0, 100, true),
    ProductWishlistUi(2, "Garmin Instinct 3 Tactical AMOLED 50mm - Black", 9772460.0, 10500000.0, 5, "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=500", "Urban Republic", "Jakarta Selatan", 4.9, 50, true),
    ProductWishlistUi(3, "Garmin Vivoactive 6 - Jasper Green", 4828060.0, 5500000.0, 12, "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500", "Garmin Indonesia", "Tangerang", 5.0, 28, true),
    ProductWishlistUi(4, "Garmin Venu 3S Soft Gold", 6899000.0, 7500000.0, 8, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500", "Dunia Gadget", "Surabaya", 4.8, 150),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistCollectionDetailScreen(
    collectionName: String,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: WishlistViewModel,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf(collectionName) }

    // State untuk Bottom Sheet (Pindah Koleksi)
    var showMoveCollectionSheet by remember { mutableStateOf(false) }
    var selectedProductForSheet by remember { mutableStateOf<ProductWishlistUi?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search in wishlist */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, "Cart")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
//                            containerColor = Color.White
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ubah Nama Koleksi") },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus Koleksi", color = TokoRed) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = TokoRed) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Filter / Sort Bar (Opsional, pemanis UI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipSmall("Urutkan")
                FilterChipSmall("Promo")
                FilterChipSmall("Kategori")
            }

            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dummyWishlistProducts) { product ->
                    ProductGridItemWithState (
                        product = product,
                        viewModel = viewModel,
                        onLoveClick = {
                            selectedProductForSheet = product
                            showMoveCollectionSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showMoveCollectionSheet && selectedProductForSheet != null) {
        SelectCollectionBottomSheet(
            onDismiss = { showMoveCollectionSheet = false },
            sheetState = sheetState,
            viewModel = viewModel,
            productToAdd = selectedProductForSheet!!
        )
    }
}

@Composable
fun ProductGridItemWithState(
    product: ProductWishlistUi,
    viewModel: WishlistViewModel,
    onLoveClick: () -> Unit
) {
    val isWishlisted by viewModel.isProductWishlisted(product.id).collectAsState(initial = false)


    ProductGridItemUI(
        product = product,
        isWishlisted = isWishlisted,
        onLoveClick = onLoveClick
    )
}
@Composable
fun ProductGridItemUI(
    product: ProductWishlistUi,
    isWishlisted: Boolean,
    onLoveClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Image & Love Icon
            Box {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // --- LOVE ICON LOGIC ---
                IconButton(
                    onClick = onLoveClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        // Jika Wishlisted: Filled Heart (Merah/Hijau), Jika Tidak: Outline
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) TokoRed else Color.Gray, // Merah jika aktif
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupiah(product.price),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                // (Sisa UI discount, shop, dll sama seperti sebelumnya)
                // ...
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, TokoGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ Keranjang", color = TokoGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCollectionBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    viewModel: WishlistViewModel,
    productToAdd: ProductWishlistUi
) {
    // Ambil data koleksi dari DB
    val collections by viewModel.collections.collectAsState()

    var isCreatingNew by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCreatingNew) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.clickable { isCreatingNew = false })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Koleksi Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Simpan ke Koleksi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Buat Baru",
                        color = TokoGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isCreatingNew = true }
                    )
                }
            }
            Divider(color = Color(0xFFF0F0F0))

            if (isCreatingNew) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        label = { Text("Nama Koleksi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.createCollection(newCollectionName)
                            isCreatingNew = false
                        },
                        enabled = newCollectionName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TokoGreen)
                    ) {
                        Text("Simpan Koleksi")
                    }
                }
            } else {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.removeFromWishlist(productToAdd.id)
                                    onDismiss()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier
                                .size(40.dp)
                                .background(Color.LightGray.copy(0.2f), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Delete, null, tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Hapus dari Wishlist", color = TokoRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    items(collections) { collection ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Tambah ke koleksi yang dipilih
                                    viewModel.addToCollection(productToAdd, collection.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier
                                .size(40.dp)
                                .background(Color.LightGray, RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(collection.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Pilih folder ini", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipSmall(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp").replace(",00", "")
}