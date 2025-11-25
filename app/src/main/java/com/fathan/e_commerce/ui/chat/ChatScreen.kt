package com.fathan.e_commerce.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.ui.home.BottomNavigationBar

// --- Enum for Navigation (Existing) ---
enum class TabChat {
    REVIEW,
    FEED,
    TRANSACTION,
    SYSTEM
}

// --- 1. Updated Data Model to match the Picture ---
data class ChatItem(
    val id: Int,
    val name: String,
    val message: String,
    val time: String,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false,
    val isOnline: Boolean = false,
    val initial: String, // Used to simulate avatar image
    val avatarColor: Color // Used to simulate avatar image
)

// --- 2. Dummy Data matching the "Messages" Image ---
val dataChatList = listOf(
    ChatItem(
        1, "Untitled Team", "John: That's a good Idea, I think we can...", "9:24 AM", 99,
        initial = "U", avatarColor = Color(0xFFE1BEE7) // Light Purple
    ),
    ChatItem(
        2, "Deemtech", "John is typing..", "9:24 AM", 0, isTyping = true,
        initial = "D", avatarColor = Color(0xFFA1887F) // Brown
    ),
    ChatItem(
        3, "Xaos Tech Enthusiast", "John: That's a good Idea, I think we can star...", "9:24 AM", 0,
        initial = "X", avatarColor = Color(0xFFB2DFDB) // Teal
    ),
    ChatItem(
        4, "Aliena", "That's a good Idea, I think we can start to", "9:24 AM", 99, isOnline = true,
        initial = "A", avatarColor = Color(0xFFFFCCBC) // Light Orange
    ),
    ChatItem(
        5, "SUSS", "John: That's a good Idea, I think we can...", "9:24 AM", 99,
        initial = "S", avatarColor = Color(0xFFC5CAE9) // Indigo
    ),
    ChatItem(
        6, "Creative Space", "John: That's a good Idea, I think we can star...", "9:24 AM", 0,
        initial = "C", avatarColor = Color(0xFFF8BBD0) // Pink
    ),
    ChatItem(
        7, "Creative Crew", "Alice: I completely agree, let's dive deeper i...", "3:30 PM", 0,
        initial = "CC", avatarColor = Color(0xFFFFE0B2) // Orange
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val navController = rememberNavController()
    // Note: Assuming you might want tabs later, keeping this logic but focusing on the List UI for now
    val startDestination = TabChat.REVIEW
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        containerColor = Color.White, // Match the clean white background
        topBar = {
            // --- 3. Custom Top Bar matching the Image ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp), // Increased padding for look
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.weight(1.0f))

                // Search Button with circle background
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5), // Very light gray background
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Black
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                selectedTab = BottomTab.CHAT,
                onProfileClick = onProfileClick,
                onCartClick = onCartClick
            )
        }
    ) { innerPadding ->

        // --- 4. Main Chat List ---
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp), // Match outer padding
            verticalArrangement = Arrangement.spacedBy(24.dp) // Spacing between items
        ) {
            items(dataChatList) { chat ->
                ChatListItem(chat = chat, onClick = onChatClick)
            }
            // Bottom spacer to prevent FAB/BottomBar overlap issues visually
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatItem, onClick : () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Avatar Section ---
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            // Simulated Image Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
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

            // Online Dot
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Green
                        .padding(2.dp) // Create white border effect
                        .background(Color.White, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- Name and Message Section ---
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (chat.isTyping) {
                Text(
                    text = chat.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF7986CB), // Periwinkle/Purple for typing
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = chat.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- Time and Badge Section ---
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = chat.time,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF5350)), // Red badge
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// --- Keep existing AppNavHost if needed for internal tab navigation later ---
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: TabChat,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.name
    ) {
        TabChat.entries.forEach { destination ->
            composable(destination.name) {
                Text(destination.name)
            }
        }
    }
}
