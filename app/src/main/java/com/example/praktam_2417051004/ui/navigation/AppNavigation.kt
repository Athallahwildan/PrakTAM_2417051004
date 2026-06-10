package com.example.praktam_2417051004.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.praktam_2417051004.ui.screen.cart.CartScreen
import com.example.praktam_2417051004.ui.screen.checkout.CheckoutScreen
import com.example.praktam_2417051004.ui.screen.detail.DetailScreen
import com.example.praktam_2417051004.ui.screen.favorite.FavoriteScreen
import com.example.praktam_2417051004.ui.screen.history.HistoryScreen
import com.example.praktam_2417051004.ui.screen.home.HomeScreen
import com.example.praktam_2417051004.ui.screen.login.LoginScreen
import com.example.praktam_2417051004.ui.screen.profile.ProfileScreen
import com.example.praktam_2417051004.ui.viewmodel.AuthViewModel
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val foodViewModel: FoodViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    // Start destination depends on login status
    val startDestination = if (authViewModel.isLoggedIn) "home" else "login"

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable("home") {
                HomeScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }

            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable("detail/{nama}") { backStackEntry ->
                val nama = backStackEntry.arguments?.getString("nama")
                val food = foodViewModel.foods.find { it.nama == nama }

                if (food != null) {
                    DetailScreen(
                        food = food,
                        navController = navController,
                        isFullScreen = true,
                        viewModel = foodViewModel
                    )
                }
            }

            composable("favorites") {
                FavoriteScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }

            composable("cart") {
                CartScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }

            composable("checkout/{total}") { backStackEntry ->
                val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
                CheckoutScreen(
                    navController = navController,
                    totalHarga = total,
                    viewModel = foodViewModel
                )
            }

            composable("history") {
                HistoryScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }
        }
    }
}
