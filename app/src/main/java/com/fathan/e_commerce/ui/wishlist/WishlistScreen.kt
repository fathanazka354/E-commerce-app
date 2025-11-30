package com.fathan.e_commerce.ui.wishlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

val TokoGreen = Color(0xFF03AC0E)

data class CollectionUi(
    val id: Int,
    val name: String,
    val itemCount: Int,
    val imageUrls: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onBack: () -> Unit,
    onCollectionClick: (Int, String) -> Unit = { _, _ -> }
) {
    val collections = remember {
        mutableStateListOf(
            CollectionUi(1, "Semua Wishlist", 10, listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500")),
            CollectionUi(2, "Mendaki", 1, listOf("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500")),
            CollectionUi(3, "Lari", 5, listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500"))
        )
    }

    // State untuk Bottom Sheet
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wishlist", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.ShoppingCart, "Cart") }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(collections, key = { it.id }) { collection ->
                CollectionCard(
                    name = collection.name,
                    count = collection.itemCount,
                    thumbnailUrl = collection.imageUrls.firstOrNull(),
                    onClick = { onCollectionClick(collection.id, collection.name) }
                )
            }

            item {
                CreateNewCollectionCard(onClick = { showBottomSheet = true })
            }
        }

        if (showBottomSheet) {
            AddCollectionBottomSheet(
                onDismiss = { showBottomSheet = false },
                onSave = { newName ->
                    // Logic menambah koleksi baru
                    val newId = (collections.maxOfOrNull { it.id } ?: 0) + 1
                    collections.add(CollectionUi(newId, newName, 0, emptyList()))
                    showBottomSheet = false
                },
                sheetState = sheetState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCollectionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    sheetState: SheetState
) {
    var collectionName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Otomatis fokus ke text field saat muncul
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null // Custom header
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                .fillMaxWidth()
                .imePadding() // Agar naik saat keyboard muncul
        ) {
            // Header Sheet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    modifier = Modifier.clickable { onDismiss() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Buat Koleksi Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Input Field
            Text("Nama Koleksi", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = collectionName,
                onValueChange = { if (it.length <= 20) collectionName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("Contoh: Barang Liburan") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TokoGreen,
                    cursorColor = TokoGreen,
                    focusedLabelColor = TokoGreen
                ),
                supportingText = {
                    Text(
                        text = "${collectionName.length}/20",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (collectionName.isNotBlank()) {
                        onSave(collectionName)
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { onSave(collectionName) },
                enabled = collectionName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TokoGreen,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Buat Koleksi", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CollectionCard(
    name: String,
    count: Int,
    thumbnailUrl: String?,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
    ) {
        // Image Container
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            if (thumbnailUrl == null) {
                // Placeholder Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(thumbnailUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info Koleksi
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count Barang",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = { /* Menu Opsi Koleksi */ }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CreateNewCollectionCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, TokoGreen, RoundedCornerShape(8.dp)) // Green border
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(Color(0xFFF0F9F0)), // Light Green bg
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Tambah",
                tint = TokoGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Koleksi Baru",
                color = TokoGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
