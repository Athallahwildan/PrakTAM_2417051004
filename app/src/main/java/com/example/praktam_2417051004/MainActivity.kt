package com.example.praktam_2417051004

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.praktam_2417051004.model.Cemil
import com.example.praktam_2417051004.network.RetrofitClient
import com.example.praktam_2417051004.ui.theme.PrakTAM_2417051004Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakTAM_2417051004Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavController) {
    var foods by remember { mutableStateOf<List<Cemil>>(emptyList()) }

    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = "home"
    ) {
        composable("home") {
            DaftarMakananScreen(navController) { fetchedFoods ->
                foods = fetchedFoods
            }
        }

        composable("detail/{nama}") { backStackEntry ->
            val nama = backStackEntry.arguments?.getString("nama")
            val food = foods.find { it.nama == nama }

            if (food != null) {
                DetailScreen(food = food, navController = navController, isFullScreen = true)
            }
        }
    }
}

@Composable
fun DaftarMakananScreen(navController: NavController, onFoodsLoaded: (List<Cemil>) -> Unit = {}) {

    var foods by remember { mutableStateOf<List<Cemil>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            foods = RetrofitClient.instance.getFoods()
            onFoodsLoaded(foods)
            isLoading = false
            isError = false
        } catch (e: Exception) {
            isLoading = false
            isError = true
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (isError || foods.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gagal Memuat Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pastikan koneksi internet Anda menyala",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                Column {

                    Text(
                        text = "Cemilan Favorit",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rekomendasi buat kamu",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(foods) { food ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable {
                                        navController.navigate("detail/${food.nama}")
                                    },
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column {
                                    AsyncImage(
                                        model = food.imageUrl,
                                        contentDescription = food.nama,
                                        placeholder = painterResource(id = R.drawable.basreng),
                                        error = painterResource(id = R.drawable.basreng),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = food.nama,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            text = "Rp ${food.harga}",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Semua Menu",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            items(foods) { food ->
                FoodItem(food = food, navController = navController)
            }
        }
    }
}

@Composable
fun FoodItem(food: Cemil, navController: NavController) {
    DetailScreen(food = food, navController = navController, isFullScreen = false)
}

@Composable
fun DetailScreen(food: Cemil, navController: NavController, isFullScreen: Boolean = false) {

    var isFavorite by remember { mutableStateOf(false) }
    var isOrderLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(4.dp, Color.Gray)
                .padding(16.dp)
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
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text(
                        text = food.nama,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = food.deskripsi,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Harga: Rp ${food.harga}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row {
                    Box {
                        AsyncImage(
                            model = food.imageUrl,
                            contentDescription = food.nama,
                            placeholder = painterResource(id = R.drawable.basreng),
                            error = painterResource(id = R.drawable.basreng),
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = food.nama,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = food.deskripsi,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Harga: Rp ${food.harga}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    if (isFullScreen) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("detail/${food.nama}")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isFullScreen) "Kembali" else "Pesan")
            }

            if (isFullScreen) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOrderLoading = true
                            delay(1500)

                            snackbarHostState.showSnackbar(
                                "Pesanan ${food.nama} siap dikirim!"
                            )

                            isOrderLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isOrderLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isOrderLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menyiapkan pesanan...")
                    } else {
                        Text("Pesan Cemilan")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}