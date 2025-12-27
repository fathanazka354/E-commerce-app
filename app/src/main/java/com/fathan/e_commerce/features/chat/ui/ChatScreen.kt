package com.fathan.e_commerce.features.chat.ui

import android.os.Build
import android.util.Log
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
import androidx.compose.material.icons.filled.Done
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
import com.fathan.e_commerce.features.chat.domain.entity.ConversationItem
import com.fathan.e_commerce.features.chat.domain.entity.MessageType
import com.fathan.e_commerce.features.chat.utils.ChatFilter
import com.fathan.e_commerce.features.components.BottomNavigationBar
import com.fathan.e_commerce.features.components.BottomTab

private val TokoGreen = Color(0xFF03AC0E)

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
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { isSearchActive = true }) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = TokoGreen)
                    }
                }
            )
        },
        bottomBar = {
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
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // Search box
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isBlank()) viewModel.updateFilter(ChatFilter.ALL) else viewModel.findChat(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        textStyle = TextStyle(color = Color.Black)
                    )
                }
            }

            // Loading
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TokoGreen)
                }
                return@Column
            }

            // Empty state
            if (uiState.displayedMessages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada percakapan", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mulai percakapan dengan pelanggan atau timmu.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                return@Column
            }


            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.displayedMessages) { conv ->
                    ChatListItem(
                        chatMessage = conv,
                        onClick = {
                            onChatOpen(conv.roomId, conv.lastSenderId ?: "")
                        }
                    )
                    Divider()
                }
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatListItem(
    chatMessage: ConversationItem,
    onClick: () -> Unit
) {
    Log.d("ChatListItem", "ChatListItem: ${chatMessage.lastMessage} || ${chatMessage.isRead} ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Avatar circle (placeholder)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Color(
                        0xFFE5E5E5
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chatMessage.otherUserName?.get(0).toString(),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chatMessage.otherUserName ?: "-",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chatMessage.isMe){
                    if (chatMessage.isRead){
                        Icon(Icons.Default.DoneAll, modifier = Modifier.size(12.dp), contentDescription = "Done", tint = Color.Blue)
                    } else{
                        Icon(Icons.Default.Done, modifier = Modifier.size(12.dp), contentDescription = "Done", tint = Color.Gray)
                    }
                }
            Spacer(modifier = Modifier.width(5.dp))
                if (chatMessage.messageType == MessageType.IMAGE){
                    Text(
                        text = "${chatMessage.otherUserName} sent an image",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = Color(0xFF8A8A8A)
                    )
                }else if (chatMessage.messageType == MessageType.AUDIO){
                    Text(
                        text = "${chatMessage.otherUserName} sent an audio",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = Color(0xFF8A8A8A)
                    )

                } else {
                    Text(
                        text = chatMessage.lastMessage ?: "-",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = Color(0xFF8A8A8A)
                    )

                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = Utils.formatTimeAgo(chatMessage.lastMessageTime ?: ""),
                fontSize = 12.sp,
                color = Color(0xFF8A8A8A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // unread badge
            if (chatMessage.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD6001C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chatMessage.unreadCount > 99) "99+" else chatMessage.unreadCount.toString(),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
