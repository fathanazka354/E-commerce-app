package com.fathan.e_commerce

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fathan.e_commerce.data.BottomTab
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.models.CartItem
import com.fathan.e_commerce.models.DummyData
import com.fathan.e_commerce.models.Product
import com.fathan.e_commerce.screens.CheckoutScreen
import com.fathan.e_commerce.screens.HomeScreen
import com.fathan.e_commerce.screens.LoginScreen
import com.fathan.e_commerce.screens.ProductDetailScreen
import com.fathan.e_commerce.screens.ProfileScreen
import com.fathan.e_commerce.screens.Screen
import com.fathan.e_commerce.screens.SearchScreen
import com.fathan.e_commerce.ui.theme.ECommerceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ECommerceTheme {
                val navController = rememberNavController()

                val prefs = remember { UserPreferences(this@MainActivity) }
                // simple app-wide states
                val isLoggedIn by prefs.isLoggedInFlow.collectAsState(initial = false)
                var cartItems by remember { mutableStateOf(listOf<CartItem>()) }

                fun addToCart(product: Product, color: String?, storage: String?) {
                    val existing = cartItems.find { it.product.id == product.id && it.selectedColor == color && it.selectedStorage == storage }
                    cartItems = if (existing != null) {
                        cartItems.map {
                            if (it == existing) it.copy(quantity = it.quantity + 1) else it
                        }
                    } else {
                        cartItems + CartItem(product, 1, color, storage)
                    }
                }

                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0)
                                    }
                                },
                                userPreferences = prefs
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                products = DummyData.products,
                                categories = DummyData.categories,
                                onProductClick = { product ->
                                    navController.navigate(Screen.Detail.createRoute(product.id))
                                },
                                onSearchClick = {
                                    Log.i("MainActivity", "onCreate: ")
                                    navController.navigate(Screen.Search.route)
                                },
                                onCartClick = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route)
                                }

                            )
                        }


                        composable(Screen.Search.route) {
                            SearchScreen(
                                onBack = { navController.popBackStack() },
                                onProductClick = { product ->
                                    navController.navigate(Screen.Detail.createRoute(product.id))
                                }
                            )
                        }


                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                preferences = prefs,
                                onBack = { navController.popBackStack() },   // atau bisa dihapus nanti
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onCartClick = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onProfileClick = { /* sudah di Profile, boleh kosong */ },
                            )
                        }

                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                            val product = DummyData.products.first { it.id == productId }

                            ProductDetailScreen(
                                product = product,
                                onBack = { navController.popBackStack() },
                                onAddToCart = { color, storage ->
                                    addToCart(product, color, storage)
                                    navController.navigate(Screen.Checkout.route)
                                }
                            )
                        }

                        composable(Screen.Checkout.route) {
                            CheckoutScreen(
                                cartItems = cartItems,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}