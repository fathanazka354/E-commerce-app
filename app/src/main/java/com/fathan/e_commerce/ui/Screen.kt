package com.fathan.e_commerce.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
    object Checkout : Screen("checkout")
    object Search : Screen("search")
    object Profile: Screen("profile")
    object Chat: Screen("chat")
    object Wishlist: Screen("wishlist")
    object ChatDetail: Screen("chat_detail{chatId}"){
        fun createRoute(chatId: Int) = "chat_detail/$chatId"
    }
}
