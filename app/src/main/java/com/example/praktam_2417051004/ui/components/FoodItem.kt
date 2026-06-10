package com.example.praktam_2417051004.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.praktam_2417051004.data.model.Cemil
import com.example.praktam_2417051004.ui.screen.detail.DetailScreen
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun FoodItem(
    food: Cemil,
    navController: NavController,
    viewModel: FoodViewModel
) {
    DetailScreen(
        food = food,
        navController = navController,
        isFullScreen = false,
        viewModel = viewModel
    )
}
