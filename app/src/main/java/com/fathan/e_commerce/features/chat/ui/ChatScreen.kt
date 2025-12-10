package com.fathan.e_commerce.features.chat.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fathan.e_commerce.Utils
import com.fathan.e_commerce.features.chat.domain.entity.ChatMessage
import com.fathan.e_commerce.features.chat.utils.ChatFilter
import com.fathan.e_commerce.features.components.BottomNavigationBar
import com.fathan.e_commerce.features.components.BottomTab

val TokoGreen = Color(0xFF03AC0E)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onChatOpen: (roomId: String, authId: String) -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onTransactionClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()),

        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = true }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = { viewModel.markAllAsRead() }) { Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = TokoGreen) }
                }
            )
        },
        bottomBar = {
            // keep your BottomNavigationBar if needed
            BottomNavigationBar(
                selectedTab = BottomTab.CHAT,
                onHomeClick = onHomeClick,
                onProfileClick = onProfileClick,
                onChatClick = onChatClick,
                onPromoClick = onTransactionClick,
                onTransactionClick = onTransactionClick
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier) {
            if (isSearchActive) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isBlank()) viewModel.updateFilter(ChatFilter.ALL) else viewModel.findChat(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                            .padding(12.dp),
                        textStyle = TextStyle(color = Color.Black)
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TokoGreen)
                }
                return@Column
            }

            // Conversation list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.displayedMessages, key = { it.id }) { conv ->
                    ChatListItem(chatMessage = conv, onClick = { onChatOpen(conv.roomId, conv.senderId?:"") })
                    Divider()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatListItem(
    chatMessage: ChatMessage,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E5E5))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chatMessage.senderName?:"-",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Text(
                text = chatMessage.message?:"-",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = Color(0xFF8A8A8A)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = Utils.formatTimeAgo(chatMessage.createdAt?:"-"),
                fontSize = 12.sp,
                color = Color(0xFF8A8A8A)
            )

            if (chatMessage.isOnline) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
