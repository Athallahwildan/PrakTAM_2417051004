package com.example.praktam_2417051004.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.praktam_2417051004.R
import com.example.praktam_2417051004.ui.components.CustomFilterChip
import com.example.praktam_2417051004.ui.components.FoodItem
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: FoodViewModel
) {
    val foods = viewModel.foods
    val isLoading = viewModel.isLoading
    val isError = viewModel.isError
    val cartFoods = viewModel.cartFoods

    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredFoods = foods.filter { food ->
        val matchSearch = food.nama.contains(searchQuery, ignoreCase = true)
        val matchCategory = when (selectedCategory) {
            "Pedas" -> food.nama.contains("pedas", true) || food.deskripsi.contains("pedas", true)
            "Original" -> food.nama.contains("original", true) || food.deskripsi.contains("original", true)
            "Manis" -> food.nama.contains("manis", true) || food.deskripsi.contains("manis", true)
            else -> true
        }
        matchSearch && matchCategory
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (isError || foods.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Gagal Memuat Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchFoods() }) {
                    Text("Coba Lagi")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(text = "Cemilan Favorit", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Rekomendasi buat kamu", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Cari Cemilan") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { navController.navigate("favorites") }) {
                            Icon(Icons.Outlined.FavoriteBorder, "Favorit")
                        }

                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(Icons.Default.Person, "Profil")
                        }

                        IconButton(onClick = { navController.navigate("history") }) {
                            Icon(Icons.AutoMirrored.Filled.List, "Riwayat")
                        }

                        IconButton(onClick = { navController.navigate("cart") }) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(Icons.Outlined.ShoppingCart, "Keranjang")
                                val uniqueCount = cartFoods.distinctBy { it.nama }.size
                                if (uniqueCount > 0) {
                                    Surface(
                                        color = Color.Red,
                                        shape = CircleShape,
                                        modifier = Modifier.size(16.dp).offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text(
                                            text = "$uniqueCount",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Semua", "Pedas", "Original", "Manis").forEach { category ->
                            item {
                                CustomFilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = category
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredFoods.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredFoods) { food ->
                                Card(
                                    modifier = Modifier.width(160.dp).clickable {
                                        navController.navigate("detail/${food.nama}")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = food.imageUrl,
                                            contentDescription = food.nama,
                                            placeholder = painterResource(id = R.drawable.basreng),
                                            error = painterResource(id = R.drawable.basreng),
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(text = food.nama, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                            Text(text = "Rp ${food.harga}", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Semua Menu", style = MaterialTheme.typography.titleLarge)
                }
            }

            if (filteredFoods.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Cemilan tidak ditemukan", color = Color.Gray)
                    }
                }
            } else {
                items(filteredFoods) { food ->
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
