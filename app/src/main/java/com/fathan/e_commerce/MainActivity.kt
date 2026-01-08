package com.fathan.e_commerce

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fathan.e_commerce.data.utils.FCMManager
import com.fathan.e_commerce.domain.model.CartItem
import com.fathan.e_commerce.features.checkout.CheckoutScreen
import com.fathan.e_commerce.features.home.HomeScreen
import com.fathan.e_commerce.features.login.LoginScreen
import com.fathan.e_commerce.features.product.ui.ProductDetailScreen
import com.fathan.e_commerce.features.profile.ProfileScreen
import com.fathan.e_commerce.features.Screen
import com.fathan.e_commerce.features.chat.ui.detail.ChatDetailScreen
import com.fathan.e_commerce.features.chat.ui.detail.ChatDetailViewModel
import com.fathan.e_commerce.features.chat.ui.list.ChatListScreen
import com.fathan.e_commerce.features.chat.ui.list.ChatListViewModel
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
import kotlinx.coroutines.delay
import javax.inject.Inject

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

    @Inject
    lateinit var fcmManager: FCMManager

    companion object {
        private const val TAG = "MainActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)
        requestNotificationPermissionIfNeeded()
        handleNotificationIntent(intent)

        setContent {
            ECommerceTheme {
                val navController = rememberNavController()
                var cartItems = emptyList<CartItem>()

                val mainViewModel: MainViewModel = hiltViewModel()
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

                // ✅ Handle deep link navigation
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
        handleNotificationIntent(intent)
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

    private fun requestNotificationPermissionIfNeeded() {
        if (!fcmManager.hasNotificationPermission(this)) {
            fcmManager.requestNotificationPermission(this)
        } else {
            fcmManager.initializeFCM(lifecycleScope)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            val shouldOpenChat = extras.getBoolean("open_chat", false)
            val conversationId = extras.getString("conversation_id")

            Log.d(TAG, "Notification intent - openChat: $shouldOpenChat, conversationId: $conversationId")

            if (shouldOpenChat && conversationId != null) {
                // ✅ Save to SharedPreferences
                getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("pending_conversation_id", conversationId)
                    .putBoolean("should_open_chat", true)
                    .apply()

                Log.d(TAG, "Saved pending navigation to SharedPreferences")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FCMManager.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission granted via callback")
                    fcmManager.initializeFCM(lifecycleScope)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    cartItems: List<CartItem>,
    onCartChanged: (List<CartItem>) -> Unit
) {
    val context = LocalContext.current

    // ✅ IMPROVED: Check for pending navigation with proper delay and state tracking
    var hasHandledPendingNavigation by remember { mutableStateOf(false) }

    LaunchedEffect(navController.currentBackStackEntry) {
        if (hasHandledPendingNavigation) return@LaunchedEffect

        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val shouldOpenChat = prefs.getBoolean("should_open_chat", false)
        val conversationId = prefs.getString("pending_conversation_id", null)

        if (shouldOpenChat && conversationId != null) {
            Log.d("MainActivity", "Opening chat from notification: $conversationId")

            // Clear the flag immediately
            prefs.edit()
                .remove("should_open_chat")
                .remove("pending_conversation_id")
                .apply()

            // ✅ Wait for navigation to be ready
            delay(1000) // Increased delay

            try {
                // Navigate directly to chat detail
                navController.navigate(Screen.ChatDetail.createRoute(conversationId)) {
                    // Clear back stack to home
                    popUpTo(Screen.Home.route) {
                        inclusive = false
                    }
                }
                hasHandledPendingNavigation = true
                Log.d("MainActivity", "Successfully navigated to chat detail")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to navigate to chat", e)
            }
        }
    }

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
            val viewModel = hiltViewModel<ChatListViewModel>()
            val lifecycleOwner = LocalLifecycleOwner.current

            // ✅ Refresh when screen becomes visible
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        Log.d("MainActivity", "ChatList screen resumed, refreshing...")
                        viewModel.refreshConversations()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            ChatListScreen(
                viewModel = viewModel,
                onConversationClick = { conversationId ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(conversationId)
                    )
                },
                onChatClick = { /* already on chat */ },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onTransactionClick = { navController.navigate(Screen.Transaction.route) }
            )
        }

        // ✅ BALANCED APPROACH: Don't over-refresh
        composable(Screen.ChatDetail.route) { backStack ->
            val viewModel = hiltViewModel<ChatDetailViewModel>()
            val lifecycleOwner = LocalLifecycleOwner.current

            // ✅ Track if this is first composition
            var hasRefreshedOnce by remember { mutableStateOf(false) }

            // ✅ CONSERVATIVE: Only refresh when truly needed
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            // ✅ Only refresh if NOT first time
                            if (hasRefreshedOnce) {
                                Log.d("MainActivity", "ChatDetail ON_RESUME (returning from background) - refreshing...")
                                viewModel.refreshMessages()
                            } else {
                                Log.d("MainActivity", "ChatDetail ON_RESUME (first time) - skipping refresh")
                                hasRefreshedOnce = true
                            }
                            viewModel.markAsRead()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            Log.d("MainActivity", "ChatDetail ON_PAUSE")
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    Log.d("MainActivity", "ChatDetail disposed, removing observer")
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    hasRefreshedOnce = false // Reset for next visit
                }
            }

            ChatDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
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

            LaunchedEffect(productId) {
                if (productId > 0) {
                    detailVM.loadProduct(productId)
                }
            }

            val uiState by detailVM.uiState.collectAsState()
            val product = uiState.product
            Log.d("MainActivity", "AppNavHost: ${product.name} || ${product.description}")

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

private fun NavHostController.safeNavigate(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute == route) return
    if (builder == null) navigate(route) else navigate(route, builder)
}