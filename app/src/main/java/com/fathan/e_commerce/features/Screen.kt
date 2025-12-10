package com.fathan.e_commerce.features

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
    object Checkout : Screen("checkout")
    object Search : Screen("search")
    object Profile: Screen("profile")
    object Wishlist: Screen("wishlist")
    object Transaction: Screen("transaction")
    object Chat : Screen("chats")
    object ChatDetailWithUser : Screen("chat_detail/{roomId}/{myAuthId}") {
        fun createRoute(roomId: String, myAuthId: String) = "chat_detail/$roomId/$myAuthId"
    }

    object Promo : Screen("promo")
    object LocalProduct : Screen("local_product")
    object FlashSale : Screen("flash_sale")
}
