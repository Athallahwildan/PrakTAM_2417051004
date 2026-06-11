package com.example.praktam_2417051004.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.praktam_2417051004.ui.screen.cart.CartScreen
import com.example.praktam_2417051004.ui.screen.checkout.CheckoutScreen
import com.example.praktam_2417051004.ui.screen.detail.DetailScreen
import com.example.praktam_2417051004.ui.screen.favorite.FavoriteScreen
import com.example.praktam_2417051004.ui.screen.history.HistoryScreen
import com.example.praktam_2417051004.ui.screen.home.HomeScreen
import com.example.praktam_2417051004.ui.screen.login.LoginScreen
import com.example.praktam_2417051004.ui.screen.profile.ProfileScreen
import com.example.praktam_2417051004.ui.screen.register.RegisterScreen
import com.example.praktam_2417051004.ui.viewmodel.AuthViewModel
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val foodViewModel: FoodViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val startDestination = if (authViewModel.isLoggedIn) Screen.Home.route else "login"

    LaunchedEffect(authViewModel.isLoggedIn, authViewModel.currentUser) {
        val user = authViewModel.currentUser
        if (authViewModel.isLoggedIn && user != null) {
            foodViewModel.setUser(user.username)
        } else {
            foodViewModel.clearUserData()
        }
    }

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Favorite,
        Screen.History,
        Screen.Profile
    )

    val showBottomBar = bottomBarScreens.any { it.route == currentDestination?.route }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    bottomBarScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = screen.icon, 
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                ) 
                            },
                            label = { 
                                Text(
                                    text = screen.title,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                ) 
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
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

            composable("register") {
                RegisterScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    foodViewModel = foodViewModel
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

            composable(Screen.Favorite.route) {
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

            composable(Screen.History.route) {
                HistoryScreen(
                    navController = navController,
                    viewModel = foodViewModel
                )
            }
        }
    }
}
