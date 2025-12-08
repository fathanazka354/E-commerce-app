package com.fathan.e_commerce.features.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.data.local.Message
import com.fathan.e_commerce.features.components.BottomTab
import com.fathan.e_commerce.features.components.BottomNavigationBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val TokoGreen = Color(0xFF03AC0E)
val TokoRed = Color(0xFFD6001C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onTransactionClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    BackHandler(isSearchActive) {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
            viewModel.searchChat("")
        } else {
            isSearchActive = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            ChatTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    viewModel.searchChat(it)
                },
                onSearchClose = {
                    isSearchActive = false
                    searchQuery = ""
                    viewModel.searchChat("")
                },
                onSearchClick = { isSearchActive = true },
                onBackClick = onBack,
                onReadAllClick = { viewModel.markAllAsRead() }
            )
        },
        bottomBar = {
            if (!isSearchActive) {
                BottomNavigationBar(
                    onHomeClick = onHomeClick,
                    selectedTab = BottomTab.CHAT,
                    onProfileClick = onProfileClick,
                    onChatClick = onChatClick,
                    onTransactionClick = onTransactionClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (!isSearchActive) {
                ChatFilterSection(
                    activeFilter = uiState.activeFilter,
                    onFilterSelected = { viewModel.updateFilter(it) }
                )
                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TokoGreen)
                }
            } else if (uiState.displayedMessages.isEmpty()) {
                EmptyChatState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.displayedMessages,
                        key = { it.id }
                    ) { chat ->
                        // Custom Swipeable Item
                        SwipeableChatItem(
                            chat = chat,
                            onClick = onChatClick,
                            onDelete = { viewModel.deleteChat(chat.id) }
                        )
                        // Divider yang tidak ikut ter-swipe
                        Divider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(start = 88.dp))
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun ChatTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
    onReadAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TopBar"
            ) { active ->
                if (active) {
                    SearchBarView(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onClose = onSearchClose
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp).clickable { onBackClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Title
                        Text(
                            text = "Chat",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Search Icon
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, "Search", tint = Color.Black)
                        }

                        // Read All (Text Action)
                        TextButton(onClick = onReadAllClick) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = TokoGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Baca Semua",
                                color = TokoGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatFilterSection(
    activeFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ChatFilter.values()) { filter ->
            val isSelected = activeFilter == filter
            val backgroundColor by animateColorAsState(
                if (isSelected) TokoGreen else Color(0xFFF5F5F5), label = "color"
            )
            val contentColor by animateColorAsState(
                if (isSelected) Color.White else Color.Gray, label = "content"
            )

            Surface(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(32.dp)
                    .clickable { onFilterSelected(filter) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = filter.label,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBarView(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close", tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp)),
            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
            cursorBrush = SolidColor(TokoGreen),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (query.isEmpty()) {
                        Text("Cari chat...", style = TextStyle(color = Color.Gray, fontSize = 16.sp))
                    }
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Close, "Clear", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyChatState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Belum ada pesan", color = Color.Gray)
        }
    }
}

@Composable
fun SwipeableChatItem(
    chat: Message,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val actionWidth = 130.dp
    val actionWidthPx = with(density) { actionWidth.toPx() }

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isRevealed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // 1. BACKGROUND ACTION (LAYER BAWAH)
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .width(actionWidth)
                .align(Alignment.CenterEnd)
                .background(TokoRed),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        scope.launch {
                            offsetX.snapTo(0f)
                            isRevealed = false
                            onDelete()
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Delete, "Hapus", tint = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hapus", fontSize = 10.sp, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.6f) // Tinggi divider 60% dari parent
                    .background(Color.White.copy(0.3f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = tween(300))
                            isRevealed = false
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Close, "Batal", tint = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Batal", fontSize = 10.sp, color = Color.White)
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .draggable(
                    state = rememberDraggableState { delta ->
                        // Hanya izinkan geser ke kiri (nilai negatif)
                        val target = offsetX.value + delta
                        scope.launch {
                            offsetX.snapTo(target.coerceIn(-actionWidthPx, 0f))
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        // Snap Logic
                        val targetOffset = if (offsetX.value < -actionWidthPx / 2) -actionWidthPx else 0f
                        isRevealed = targetOffset != 0f
                        scope.launch {
                            offsetX.animateTo(targetOffset, animationSpec = tween(300))
                        }
                    }
                )
                .clickable {
                    if (isRevealed) {
                        scope.launch {
                            offsetX.animateTo(0f)
                            isRevealed = false
                        }
                    } else {
                        onClick()
                    }
                }
        ) {
            ChatListItem(chat)
        }
    }
}

@Composable
fun ChatListItem(chat: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(chat.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(TokoGreen)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.senderName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.isTyping) {
                    Text(
                        text = "Sedang mengetik...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TokoGreen,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = chat.message?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (chat.unreadCount > 0) Color.Black else Color.Gray,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(TokoRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99" else chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
