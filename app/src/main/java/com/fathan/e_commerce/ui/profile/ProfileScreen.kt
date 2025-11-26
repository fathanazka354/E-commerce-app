package com.fathan.e_commerce.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.ui.home.BottomNavigationBar
import com.fathan.e_commerce.ui.theme.BlueSoftBackground
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onBack: ()-> Unit,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistClick: () -> Unit
) {
    val name by profileViewModel.name.collectAsState()
    val email by profileViewModel.email.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                onCartClick = onCartClick,
                onProfileClick = onProfileClick,
                selectedTab = BottomTab.PROFILE,
                onWishlistClick = onWishlistClick,
                onChatClick = onChatClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(BlueSoftBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Back button (optional, boleh kamu hapus kalau mau full tab only)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp).clickable { onBack() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Profile", // Or "Wishlist" per your request
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .align(Alignment.CenterHorizontally)
            )

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )


            Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAction("My Orders", "📄")
            ProfileAction("Coupons", "🏷️")
            ProfileAction("Wishlist", "❤️")
        }

        Spacer(Modifier.height(24.dp))

        Text("Account Settings", fontWeight = FontWeight.Bold)

        ProfileMenu("Payment Methods", "💳")
        ProfileMenu("Address", "📍")
        ProfileMenu("Security & Password", "🔒")

        Spacer(Modifier.height(24.dp))
        Text("Help & Info", fontWeight = FontWeight.Bold)

        ProfileMenu("Help & Support", "❓")
        ProfileMenu("Privacy Policy", "🛡️")
        ProfileMenu("About App", "ℹ️")

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                profileViewModel.logout()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Log Out", color = Color.White)
        }
    }
    }

}

@Composable
fun ProfileAction(label: String, icon: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(100.dp)
            .clickable { }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}

@Composable
fun ProfileMenu(label: String, icon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))
        Text(label, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text("›", fontSize = 20.sp)
    }
}