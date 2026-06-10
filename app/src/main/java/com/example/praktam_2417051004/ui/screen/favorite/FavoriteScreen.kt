package com.example.praktam_2417051004.ui.screen.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.praktam_2417051004.ui.components.CustomTopAppBar
import com.example.praktam_2417051004.ui.components.FoodItem
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun FavoriteScreen(
    navController: NavController,
    viewModel: FoodViewModel
) {
    val favoriteFoods = viewModel.favoriteFoods

    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Favorit Saya", onBack = { navController.popBackStack() })
        }
    ) { paddingValues ->
        if (favoriteFoods.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Belum ada cemilan favorit", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteFoods) { food ->
                    FoodItem(
                        food = food,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
