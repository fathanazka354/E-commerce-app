package com.fathan.e_commerce.features.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 1. Reusable Consistent AppBar ---
@Composable
fun ECommerceTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onCartClick: () -> Unit,
    onChatClick: () -> Unit,
    onBackClick: (() -> Unit)? = null // Optional back button
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Search Bar
        Surface(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(placeholder, color = Color.Gray, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Icons with Badges (Static for UI)
        Box {
            Icon(Icons.Outlined.Chat, "Chat", modifier = Modifier
                .size(24.dp)
                .clickable { onChatClick() }, tint = Color.DarkGray)
            // Badge Example
            Box(modifier = Modifier.align(Alignment.TopEnd).size(8.dp).background(Color.Red, androidx.compose.foundation.shape.CircleShape))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box {
            Icon(Icons.Outlined.ShoppingCart, "Cart", modifier = Modifier
                .size(24.dp)
                .clickable { onCartClick() }, tint = Color.DarkGray)
        }
    }
}
