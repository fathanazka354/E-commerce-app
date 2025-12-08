package com.fathan.e_commerce.features.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: BottomTab,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTransactionClick: () -> Unit = {},
    onPromoClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp), // Handle system navigation bar
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // Increased height for better touch targets
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Filled.Home,
                outlinedIcon = Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedTab == BottomTab.HOME,
                onClick = onHomeClick
            )

            BottomNavItem(
                icon = Icons.Filled.Message,
                outlinedIcon = Icons.Outlined.Message,
                label = "Chat",
                isSelected = selectedTab == BottomTab.CHAT,
                onClick = onChatClick
            )

            BottomNavItem(
                icon = Icons.Filled.Discount,
                outlinedIcon = Icons.Outlined.Discount,
                label = "Promo",
                isSelected = selectedTab == BottomTab.PROMO,
                onClick = onPromoClick
            )

            BottomNavItem(
                icon = Icons.Filled.ReceiptLong,
                outlinedIcon = Icons.Outlined.ReceiptLong,
                label = "Transaksi",
                isSelected = selectedTab == BottomTab.TRANSACTION,
                onClick = onTransactionClick
            )

            BottomNavItem(
                icon = Icons.Filled.Person,
                outlinedIcon = Icons.Outlined.Person,
                label = "Akun",
                isSelected = selectedTab == BottomTab.PROFILE,
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    outlinedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokoGreen = Color(0xFF03AC0E)
    val unselectedGray = Color(0xFF9E9E9E)

    // Animated colors
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) tokoGreen else unselectedGray,
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) tokoGreen else unselectedGray,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    // Animated scale
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                indication = rememberRipple(
                    bounded = true,
                    radius = 40.dp,
                    color = tokoGreen
                ),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with background indicator
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Background circle for selected item
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = tokoGreen.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                )
            }

            Icon(
                imageVector = if (isSelected) icon else outlinedIcon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            maxLines = 1
        )
    }
}