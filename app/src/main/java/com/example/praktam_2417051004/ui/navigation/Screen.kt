package com.example.praktam_2417051004.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Favorite : Screen("favorites", "Favorit", Icons.Default.Favorite)
    object History : Screen("history", "Riwayat", Icons.AutoMirrored.Filled.List)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
}
