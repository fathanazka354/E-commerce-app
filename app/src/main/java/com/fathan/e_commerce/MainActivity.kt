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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fathan.e_commerce.domain.model.CartItem
import com.fathan.e_commerce.features.checkout.CheckoutScreen
import com.fathan.e_commerce.features.home.HomeScreen
import com.fathan.e_commerce.features.login.LoginScreen
import com.fathan.e_commerce.features.product.ui.ProductDetailScreen
import com.fathan.e_commerce.features.profile.ProfileScreen
import com.fathan.e_commerce.features.Screen
import com.fathan.e_commerce.features.chat.ui.ChatDetailScreen
import com.fathan.e_commerce.features.chat.ui.ChatScreen
import com.fathan.e_commerce.features.forgot_password.ForgotPasswordScreen
import com.fathan.e_commerce.features.home.HomeViewModel
import com.fathan.e_commerce.features.login.LoginViewModel
import com.fathan.e_commerce.features.product.ui.ProductDetailViewModel
import com.fathan.e_commerce.features.profile.ProfileViewModel
import com.fathan.e_commerce.features.promo.PromoFlashSaleScreen
import com.fathan.e_commerce.features.promo.PromoLocalScreen
import com.fathan.e_commerce.features.promo.PromoScreen
import com.fathan.e_commerce.features.reset_password.ResetPasswordScreen
import com.fathan.e_commerce.features.search.SearchScreen
import com.fathan.e_commerce.features.search.SearchViewModel
import com.fathan.e_commerce.features.signup.SignUpScreen
import com.fathan.e_commerce.features.theme.ECommerceTheme
import com.fathan.e_commerce.features.transaction.TransactionScreen
import com.fathan.e_commerce.features.wishlist.WishlistCollectionDetailScreen
import com.fathan.e_commerce.features.wishlist.WishlistScreen
import com.fathan.e_commerce.features.wishlist.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint

object TokenHolder {
    var accessToken: String? = null
    var refreshToken: String? = null
    var shouldNavigateToReset: Boolean = false

    fun clear() {
        accessToken = null
        refreshToken = null
        shouldNavigateToReset = false
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle initial deep link
        handleDeepLink(intent)

        setContent {
            ECommerceTheme {
                val navController = rememberNavController()

                // FIX: provide plain List as initial value (not mutableStateOf)
                var cartItems = emptyList<CartItem>()      // Alternative correct init:
                // var cartItems by rememberSaveable(saver = CartItemListSaver) { listOf<CartItem>() }

                // Main VM
                val mainViewModel: MainViewModel = hiltViewModel()
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

                // If deep link set shouldNavigateToReset, navigate once when nav is ready
                LaunchedEffect(navController, TokenHolder.shouldNavigateToReset) {
                    if (TokenHolder.shouldNavigateToReset) {
                        Log.d(TAG, "Navigate to reset-password due to deep link")
                        navController.safeNavigate("reset-password") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        }
                        TokenHolder.shouldNavigateToReset = false
                    }
                }

                AppNavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
                    cartItems = cartItems,
                    onCartChanged = { cartItems = it }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data == null) {
            Log.d(TAG, "No deep link present")
            return
        }

        Log.d(TAG, "Deep link: $data")
        val scheme = data.scheme
        val host = data.host
        val type = data.getQueryParameter("type")
        val accessToken = data.getQueryParameter("access_token")
        val refreshToken = data.getQueryParameter("refresh_token")

        if (scheme == "myapp" && host == "reset-password" && type == "recovery" && !accessToken.isNullOrBlank()) {
            TokenHolder.accessToken = accessToken
            TokenHolder.refreshToken = refreshToken ?: ""
            TokenHolder.shouldNavigateToReset = true
            Log.d(TAG, "Parsed reset-password deep link, tokens saved to TokenHolder")
        } else {
            Log.d(TAG, "Deep link not recognized")
        }
    }
}

/* Nav host (kept same, but ProductDetailScreen call fixed) */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    cartItems: List<CartItem>,
    onCartChanged: (List<CartItem>) -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding()) {
        composable(Screen.Login.route) {
            val loginVM: LoginViewModel = hiltViewModel()
            LoginScreen(
                loginViewModel = loginVM,
                onLoginSuccess = {
                    navController.safeNavigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onAlreadyHaveAccountClick = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.safeNavigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable("reset-password") {
            val accessToken = TokenHolder.accessToken ?: ""
            val refreshToken = TokenHolder.refreshToken ?: ""
            ResetPasswordScreen(
                accessToken = accessToken,
                refreshToken = refreshToken,
                onDone = {
                    TokenHolder.clear()
                    navController.safeNavigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                },
                onBackToLoginClick = {
                    TokenHolder.clear()
                    navController.safeNavigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeVM: HomeViewModel = hiltViewModel()
            HomeScreen(
                homeViewModel = homeVM,
                onProductClick = { productId ->
                    Log.d("MainActivity", "AppNavHost: ${productId}")
                    navController.navigate(Screen.Detail.createRoute(productId))
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onCartClick = { navController.navigate(Screen.Checkout.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onTransactionClick = { navController.navigate(Screen.Transaction.route) },
                onPromoClick = { navController.navigate(Screen.Promo.route) }
            )
        }

        composable(Screen.Search.route) {
            val searchVM: SearchViewModel = hiltViewModel()
            SearchScreen(
                searchViewModel = searchVM,
                onBack = { navController.popBackStack() },
                onProductClick = { p -> navController.navigate(Screen.Detail.createRoute(p.id)) }
            )
        }

        composable(Screen.Promo.route) {
            PromoScreen(
                onHomeClick = { navController.navigate(Screen.Home.route) },
                onTransactionClick = { navController.navigate(Screen.Transaction.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onCartClick = { navController.navigate(Screen.Checkout.route) },
                onLocalProductClick = { navController.navigate(Screen.LocalProduct.route) },
                onFlashSaleClick = { navController.navigate(Screen.FlashSale.route) }
            )
        }

        composable(Screen.LocalProduct.route) {
            PromoLocalScreen(onBack = { navController.popBackStack() }, onCartClick = { navController.navigate(Screen.Checkout.route) })
        }

        composable(Screen.FlashSale.route) {
            PromoFlashSaleScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.safeNavigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onTransactionClick = { navController.navigate(Screen.Transaction.route) },
                onChatOpen = { roomId, authId ->
                    // navigate to detail and pass room id
                    navController.safeNavigate(Screen.ChatDetailWithUser.createRoute(roomId, authId))
                }
            )
        }

        composable(
            route = Screen.ChatDetailWithUser.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("myAuthId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            val myAuthId = backStackEntry.arguments?.getString("myAuthId") ?: return@composable
            ChatDetailScreen(roomId = roomId, myAuthId = myAuthId, onBack = { navController.popBackStack() })
        }

        composable(Screen.Transaction.route) {
            TransactionScreen(
                onHomeClick = {
                    navController.safeNavigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false } }
                },
                onCartClick = { navController.navigate(Screen.Checkout.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) }
            )
        }

        composable(Screen.Wishlist.route) {
            WishlistScreen(onBack = { navController.popBackStack() }, onCollectionClick = { id, name ->
                navController.navigate("wishlist_detail/$name")
            })
        }

        composable(
            route = "wishlist_detail/{collectionName}",
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

        composable(Screen.Profile.route) {
            val profileVM: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                profileViewModel = profileVM,
                onBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.safeNavigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false } }
                },
                onTransactionClick = { navController.navigate(Screen.Transaction.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onWishlistClick = { navController.navigate(Screen.Wishlist.route) },
                onLogoutNavigateToLogin = {
                    navController.safeNavigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val detailVM: ProductDetailViewModel = hiltViewModel()
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0

            // Trigger load only when we have a valid productId
            LaunchedEffect(productId) {
                if (productId > 0) {
                    detailVM.loadProduct(productId)   // <-- load here, NOT inside Screen
                }
            }
            // For now we reuse DummyData; in production fetch inside detailVM by productId
//            val product = DummyData.products.firstOrNull { it.id == productId } ?: DummyData.products.first()
            val uiState by detailVM.uiState.collectAsState()

            // Product data fallback (for UI that expects a Product object immediately)
            val product = uiState.product
            Log.d("MainActivity", "AppNavHost: ${product.name} || ${product.description}")
            // --- FIX: pass correct params that match your ProductDetailScreen signature ---

            ProductDetailScreen(
                uiState = uiState,
                viewModel = detailVM,
                onBack = { navController.popBackStack() },
                onAddToCart = { productIdArg, variantId, qty ->
                    val p = uiState.product
                    val newItem = CartItem(p, qty, selectedColor = null, selectedStorage = null)
                    onCartChanged(cartItems + newItem)
                    navController.navigate(Screen.Checkout.route)
                },
                onBuyWithPromo = { _, _, _ ->
                    navController.navigate(Screen.Checkout.route)
                },
                onOpenFeedback = { pid -> /* navigate to feedback list if needed */ },
                onOpenProduct = { pid -> navController.navigate(Screen.Detail.createRoute(pid)) }
            )
        }

        composable(Screen.Checkout.route) {
            CheckoutScreen(
                cartItems = cartItems,
                onBack = { navController.popBackStack() },
                onRemoveItem = { itemToRemove -> onCartChanged(cartItems.filter { it != itemToRemove }) },
                onUpdateQuantity = { item, newQty ->
                    onCartChanged(cartItems.map { if (it == item) it.copy(quantity = newQty) else it })
                }
            )
        }
    }
}

/* helper safe navigation to avoid duplicate navigation */
private fun NavHostController.safeNavigate(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute == route) return
    if (builder == null) navigate(route) else navigate(route, builder)
}

/* Saver for rememberSaveable cart list (simple) */
//private val CartItemListSaver = run {
//    listSaver<List<CartItem>, String>(
//        save = { list -> listOf(list.joinToString("|") { "${it.product.id},${it.quantity}" }) },
//        restore = { serialized ->
//            if (serialized.isEmpty()) return@listSaver emptyList()
//            val pairs = serialized.toString().split("|")
//            pairs.mapNotNull {
//                val parts = it.split(",")
//                val pid = parts.getOrNull(0)?.toIntOrNull()
//                val qty = parts.getOrNull(1)?.toIntOrNull() ?: 1
//                val product = DummyData.products.firstOrNull { p -> p.id == pid } ?: return@mapNotNull null
//                CartItem(product = product, quantity = qty, selectedColor = null, selectedStorage = null)
//            }
//        }
//    )
//}
