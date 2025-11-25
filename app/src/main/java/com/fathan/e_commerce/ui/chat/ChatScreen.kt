package com.fathan.e_commerce.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fathan.e_commerce.ui.components.BottomTab
import com.fathan.e_commerce.ui.home.BottomNavigationBar

enum class TabChat {
    REVIEW,
    FEED,
    TRANSACTION,
    SYSTEM
}

data class Chat(val id: Int, val name: String,val message: String)

val dataChatList = listOf(
    Chat(1, "Fathan", "Halo"),
    Chat(2, "Azka", "Hi"),
    Chat(3, "Fathan", "Nice to meet you"),
    Chat(4, "Azka", "Nice to meet you too"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: ()-> Unit,
    onHomeClick: ()-> Unit,
    onCartClick: ()-> Unit,
    onProfileClick: ()-> Unit,
) {
    val navController = rememberNavController()
    val startDestination = TabChat.REVIEW
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                selectedTab = BottomTab.CHAT,
                onProfileClick = onProfileClick,
                onCartClick = onCartClick
            )
        }
    ) {
        innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(dataChatList.size) {
                Row() {
                Text(text = dataChatList[it].name, )
                    Text(" - ")
                Text(text = dataChatList[it].message, )

                }
            }
        }
    }
}




//        Column() {
//
//            PrimaryTabRow(selectedTabIndex = selectedDestination, modifier = Modifier.padding(innerPadding)) {
//                TabChat.entries.forEachIndexed { index, chat ->
//                    Tab(
//                        selected = selectedDestination == index,
//                        onClick = {
//                            navController.navigate(chat.name)
//                            selectedDestination = index
//                        },
//                        text = {
//                            Text(
//                                text = chat.name,
//                                maxLines = 2,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    )
//                }
//            }
//            AppNavHost(navController = navController, startDestination = startDestination)
//        }
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
                when (destination) {
                    TabChat.REVIEW -> {
                        Text("Review")
                    }
                    TabChat.TRANSACTION -> {
                        Text("Transaction")
                    }
                    TabChat.SYSTEM -> {
                        Text("System")
                    }
                    TabChat.FEED -> {
                        Text("Feed")
                    }
                }
            }
        }
    }
}