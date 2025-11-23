package com.fathan.e_commerce.screens

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
    object Checkout : Screen("checkout")
    object Search : Screen("search")
    object Profile: Screen("profile")
}
