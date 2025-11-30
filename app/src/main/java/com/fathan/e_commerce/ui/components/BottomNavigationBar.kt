package com.fathan.e_commerce.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fathan.e_commerce.ui.components.BottomTab

// Definisikan Enum dengan icon Outline & Filled
enum class BottomTabInfo(
    val tab: BottomTab,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    HOME(BottomTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    FEED(BottomTab.CHAT, "Chat", Icons.Filled.Message, Icons.Outlined.Message),
    PROMO(BottomTab.PROMO, "Promo", Icons.Filled.Discount, Icons.Outlined.Discount),
    // Ubah Wishlist menjadi Transaksi (Receipt/List Icon)
    TRANSACTION(BottomTab.TRANSACTION, "Transaksi", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    PROFILE(BottomTab.PROFILE, "Akun", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BottomNavigationBar(
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit, // (Opsional jika tidak dipakai di bar)
    onProfileClick: () -> Unit,
    onTransactionClick: () -> Unit = {}, // Sekarang ini untuk Transaksi
    selectedTab: BottomTab
) {
    val TokoGreen = Color(0xFF03AC0E)

    NavigationBar(
        containerColor = Color.White
    ) {
        BottomTabInfo.entries.forEach { tabInfo ->
            val isSelected = selectedTab == tabInfo.tab

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    when (tabInfo.tab) {
                        BottomTab.HOME -> onHomeClick()
                        BottomTab.CHAT -> onChatClick() // Todo
                        BottomTab.PROMO -> {} // Todo
                        BottomTab.TRANSACTION -> onTransactionClick() // Navigasi ke Transaksi
                        BottomTab.PROFILE -> onProfileClick()
                        else -> {}
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tabInfo.iconFilled else tabInfo.iconOutlined,
                        contentDescription = tabInfo.label
                    )
                },
                label = {
                    Text(
                        text = tabInfo.label,
                        color = if (isSelected) TokoGreen else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TokoGreen,
                    selectedTextColor = TokoGreen,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent // Hilangkan background pill default M3
                )
            )
        }
    }
}
