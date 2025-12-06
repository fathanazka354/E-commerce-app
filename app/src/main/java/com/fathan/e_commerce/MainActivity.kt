package com.fathan.e_commerce

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.data.utils.extractTokenFromIntent
import com.fathan.e_commerce.domain.model.CartItem
import com.fathan.e_commerce.domain.model.DummyData
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.ui.checkout.CheckoutScreen
import com.fathan.e_commerce.ui.home.HomeScreen
import com.fathan.e_commerce.ui.login.LoginScreen
import com.fathan.e_commerce.ui.product.ProductDetailScreen
import com.fathan.e_commerce.ui.profile.ProfileScreen
import com.fathan.e_commerce.ui.Screen
import com.fathan.e_commerce.ui.chat.ChatDetailScreen
import com.fathan.e_commerce.ui.chat.ChatScreen
import com.fathan.e_commerce.ui.forgot_password.ForgotPasswordScreen
import com.fathan.e_commerce.ui.home.HomeViewModel
import com.fathan.e_commerce.ui.login.LoginViewModel
import com.fathan.e_commerce.ui.product.ProductDetailViewModel
import com.fathan.e_commerce.ui.profile.ProfileViewModel
import com.fathan.e_commerce.ui.promo.PromoFlashSaleScreen
import com.fathan.e_commerce.ui.promo.PromoLocalScreen
import com.fathan.e_commerce.ui.promo.PromoScreen
import com.fathan.e_commerce.ui.reset_password.ResetPasswordScreen
import com.fathan.e_commerce.ui.search.SearchScreen
import com.fathan.e_commerce.ui.search.SearchViewModel
import com.fathan.e_commerce.ui.signup.SignUpScreen
import com.fathan.e_commerce.ui.theme.ECommerceTheme
import com.fathan.e_commerce.ui.transaction.TransactionScreen
import com.fathan.e_commerce.ui.wishlist.WishlistCollectionDetailScreen
import com.fathan.e_commerce.ui.wishlist.WishlistScreen
import com.fathan.e_commerce.ui.wishlist.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.Int

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ECommerceTheme {
                navController = rememberNavController()

                LaunchedEffect(Unit) {
                    handleIntentIfDeepLink(intent)
                }

                val mainViewModel: MainViewModel = hiltViewModel()

                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

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
                            // ✅ Created here: Scope is limited to Login Screen
                            val loginVM: LoginViewModel = hiltViewModel()

                            LoginScreen(
                                loginViewModel = loginVM,
                                onLoginSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0)
                                    }
                                },
                                onSignUpClick = {
                                    navController.navigate(Screen.SignUp.route)
                                },
                                onForgotPasswordClick = {
                                    navController.navigate(Screen.ForgotPassword.route)
                                }
                            )
                        }

                        composable(Screen.SignUp.route) {
                            SignUpScreen(
                                onBackClick = { navController.popBackStack() },
                                onAlreadyHaveAccountClick = { navController.popBackStack() },
                                onSignUpSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        composable(Screen.ForgotPassword.route) {
                            ForgotPasswordScreen(
                                onBackClick = { navController.popBackStack() },
                            )
                        }

                        composable(
                            route = "reset_password?token={token}",
                            arguments = listOf(navArgument("token") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }),
                            deepLinks = listOf(navDeepLink { uriPattern = "myapp://reset-password?access_token={token}" })
                        ) { backStackEntry ->
                            val tokenArg = backStackEntry.arguments?.getString("token")
                            ResetPasswordScreen(
                                token = tokenArg,
                                onDone = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }



                        composable(Screen.Home.route) {
                            // ✅ Created here
                            val homeVM: HomeViewModel = hiltViewModel()

                            HomeScreen(
                                homeViewModel = homeVM,
                                categories = DummyData.categories,
                                onProductClick = { product ->
                                    navController.navigate(Screen.Detail.createRoute(product.id))
                                },
                                onSearchClick = {
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
                                },
                                onChatClick = {
                                    Log.d("TAG", "onCreate: HAHAHA")
                                    navController.navigate(Screen.Chat.route)
                                },
                                onTransactionClick = {
                                    navController.navigate(Screen.Transaction.route)
                                },
                                onPromoClick = {
                                    navController.navigate(Screen.Promo.route)
                                }
                            )
                        }

                        composable(Screen.Search.route) {
                            // ✅ Created here
                            val searchVM: SearchViewModel = hiltViewModel()

                            SearchScreen(
                                searchViewModel = searchVM,
                                onBack = { navController.popBackStack() },
                                onProductClick = { product ->
                                    navController.navigate(Screen.Detail.createRoute(product.id))
                                }
                            )
                        }

                        composable(Screen.Promo.route) { // Route Promo Utama
                            PromoScreen(
                                onHomeClick = { navController.navigate(Screen.Home.route) },
                                onTransactionClick = { navController.navigate(Screen.Transaction.route) },
                                onProfileClick = { navController.navigate(Screen.Profile.route) },
                                onCartClick = { navController.navigate(Screen.Checkout.route) },
                                onLocalProductClick = { navController.navigate(Screen.LocalProduct.route) }, // Navigasi ke Lokal
                                onFlashSaleClick = { navController.navigate(Screen.FlashSale.route) } // Navigasi ke Flash Sale
                            )
                        }

                        composable(Screen.LocalProduct.route) {
                            PromoLocalScreen(
                                onBack = { navController.popBackStack() },
                                onCartClick = {
                                    navController.navigate(Screen.Checkout.route)
                                }
                            )
                        }


                        composable(Screen.FlashSale.route) {
                            PromoFlashSaleScreen(
                                onBack = { navController.popBackStack() },

                            )
                        }
                        composable(Screen.Chat.route) {
                            ChatScreen( onBack = { navController.popBackStack() },
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onChatClick = {
                                    navController.navigate(Screen.ChatDetail.route)
                                },
                                onTransactionClick = {
                                    navController.navigate(Screen.Transaction.route)
                                }
                            )
                        }

                        composable(Screen.ChatDetail.route) {
                            ChatDetailScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ... di dalam NavHost
                        composable(Screen.Transaction.route) { // Route 'wishlist' sekarang menampilkan halaman Transaksi
                            TransactionScreen(
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onCartClick = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onChatClick = {
                                    navController.navigate(Screen.Chat.route)
                                }
                            )
                        }


                        // In your NavHost definition
                        composable(Screen.Wishlist.route) {
                            WishlistScreen(
                                onBack = { navController.popBackStack() },
                                onCollectionClick = { id, name ->
                                    // Navigasi ke halaman detail baru
                                    navController.navigate("wishlist_detail/$name")
                                }
                            )
                        }


                        composable(Screen.Profile.route) {
                            val profileVM: ProfileViewModel = hiltViewModel()

                            ProfileScreen(
                                profileViewModel = profileVM,
                                onBack = { navController.popBackStack() },
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onTransactionClick = {
                                    navController.navigate(Screen.Transaction.route)
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onChatClick = {
                                    navController.navigate(Screen.Chat.route)
                                },
                                onWishlistClick = {
                                    navController.navigate(Screen.Wishlist.route)
                                },
                                onLogoutNavigateToLogin = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val detailVM: ProductDetailViewModel = hiltViewModel()

                            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                            val product = DummyData.products.first { it.id == productId }

                            ProductDetailScreen(
                                productDetailViewModel = detailVM,
                                product = product,
                                onBack = { navController.popBackStack() },
                                onAddToCart = { color, storage ->
                                    addToCart(product, color, storage)
                                    navController.navigate(Screen.Checkout.route)
                                }
                            )
                        }


                        composable(
                            route = "wishlist_detail/{collectionName}", // Definisikan argument
                            arguments = listOf(navArgument("collectionName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val collectionName = backStackEntry.arguments?.getString("collectionName") ?: "Wishlist"
                            val wishlistVM: WishlistViewModel = hiltViewModel()

                            WishlistCollectionDetailScreen(
                                collectionName = collectionName,
                                onBack = { navController.popBackStack() },
                                viewModel = wishlistVM,
                                onCartClick = { navController.navigate(Screen.Checkout.route) }
                            )
                        }


                        composable(Screen.Checkout.route) {
                            CheckoutScreen(
                                cartItems = cartItems,
                                onBack = { navController.popBackStack() },
                                onRemoveItem = { itemToRemove ->
                                    cartItems = cartItems.filter { it != itemToRemove }
                                },
                                onUpdateQuantity = { item, newQty ->
                                    cartItems = cartItems.map {
                                        if (it == item) it.copy(quantity = newQty) else it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleIntentIfDeepLink(intent: Intent?) {
        val token = extractTokenFromIntent(intent)
        if (!token.isNullOrBlank()) {
            // navigate to reset screen with token
            navigateToReset(token)
        } else {
            Log.d("MainActivity", "handleIntent: no token found")
        }
    }

    private fun navigateToReset(token: String) {
        val encoded = Uri.encode(token)
        val route = "reset_password?token=$encoded"

        navController.navigate(route) {
            launchSingleTop = true
            restoreState = false
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
        }

    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let {
            if (::navController.isInitialized) {
                navController.handleDeepLink(it)
            } else {
                Log.w("MainActivity", "navController not initialized yet - deep link ignored")
            }
        }
    }
}
