package com.example.praktam_2417051004.ui.screen.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.praktam_2417051004.R
import com.example.praktam_2417051004.data.model.Cemil
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    food: Cemil,
    navController: NavController,
    isFullScreen: Boolean = false,
    viewModel: FoodViewModel
) {
    val favoriteFoods = viewModel.favoriteFoods
    val isFavorite = favoriteFoods.any { it.nama == food.nama }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(4.dp, Color.Gray)
                .padding(if (isFullScreen) 0.dp else 16.dp)
        ) {
            if (isFullScreen) {
                Box {
                    AsyncImage(
                        model = food.imageUrl,
                        contentDescription = food.nama,
                        placeholder = painterResource(id = R.drawable.basreng),
                        error = painterResource(id = R.drawable.basreng),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(food) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = food.nama, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = food.deskripsi, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Ukuran: ${food.ukuran}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Rp ${food.harga}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            } else {
                Row {
                    Box {
                        AsyncImage(
                            model = food.imageUrl,
                            contentDescription = food.nama,
                            placeholder = painterResource(id = R.drawable.basreng),
                            error = painterResource(id = R.drawable.basreng),
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.toggleFavorite(food) },
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = food.nama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = food.deskripsi, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Ukuran: ${food.ukuran}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Rp ${food.harga}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFullScreen) 16.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isFullScreen) {
                            navController.navigate("checkout/${food.harga}")
                        } else {
                            navController.navigate("detail/${food.nama}")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isFullScreen) "Pesan Sekarang" else "Pesan")
                }

                OutlinedIconButton(
                    onClick = {
                        viewModel.addToCart(food)
                        scope.launch {
                            snackbarHostState.showSnackbar("${food.nama} ditambah ke keranjang")
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Tambah ke Keranjang")
                }
            }
        }

        if (isFullScreen) {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
