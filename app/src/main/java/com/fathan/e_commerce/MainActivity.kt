package com.fathan.e_commerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.domain.model.CartItem
import com.fathan.e_commerce.domain.model.DummyData
import com.fathan.e_commerce.domain.model.Product
import com.fathan.e_commerce.ui.checkout.CheckoutScreen
import com.fathan.e_commerce.ui.home.HomeScreen
import com.fathan.e_commerce.ui.login.LoginScreen
import com.fathan.e_commerce.ui.product.ProductDetailScreen
import com.fathan.e_commerce.ui.profile.ProfileScreen
import com.fathan.e_commerce.ui.Screen
import com.fathan.e_commerce.ui.home.HomeViewModel
import com.fathan.e_commerce.ui.login.LoginViewModel
import com.fathan.e_commerce.ui.product.ProductDetailViewModel
import com.fathan.e_commerce.ui.profile.ProfileViewModel
import com.fathan.e_commerce.ui.search.SearchScreen
import com.fathan.e_commerce.ui.search.SearchViewModel
import com.fathan.e_commerce.ui.theme.ECommerceTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ECommerceTheme {
                val navController = rememberNavController()

                // MOVED: ViewModels removed from here.
                // We create them inside the specific screens below.
                // ✅ ADD THIS: Get the MainViewModel
                val mainViewModel: MainViewModel = hiltViewModel()

                // ✅ Observe state safely from the ViewModel
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
                                userPreferences = mainViewModel.prefs
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

                        composable(Screen.Profile.route) {
                            // ✅ Created here
                            val profileVM: ProfileViewModel = hiltViewModel()

                            ProfileScreen(
                                profileViewModel = profileVM,
                                onBack = { navController.popBackStack() },
                                onHomeClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                },
                                onCartClick = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onProfileClick = { }
                            )
                        }

                        composable(
                            route = Screen.Detail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            // ✅ This is CRITICAL.
                            // Creating the VM here ensures it gets the arguments from backStackEntry automatically via SavedStateHandle
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
