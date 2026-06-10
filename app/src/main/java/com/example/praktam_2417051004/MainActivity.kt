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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
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
import com.example.praktam_2417051004.data.model.Cemil
import com.example.praktam_2417051004.data.api.RetrofitClient
import com.example.praktam_2417051004.ui.theme.PrakTAM_2417051004Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.praktam_2417051004.data.repository.FoodRepository

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
    var favoriteFoods by remember { mutableStateOf<List<Cemil>>(emptyList()) }

    val onFavoriteClick: (Cemil) -> Unit = { selectedFood ->
        favoriteFoods = if (favoriteFoods.contains(selectedFood)) {
            favoriteFoods - selectedFood
        } else {
            favoriteFoods + selectedFood
        }
    }

    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = "home"
    ) {
        composable("home") {
            DaftarMakananScreen(
                navController = navController,
                favoriteFoods = favoriteFoods,
                onFavoriteClick = onFavoriteClick,
                onFoodsLoaded = { fetchedFoods ->
                    foods = fetchedFoods
                }
            )
        }

        composable("detail/{nama}") { backStackEntry ->
            val nama = backStackEntry.arguments?.getString("nama")
            val food = foods.find { it.nama == nama }

            if (food != null) {
                DetailScreen(
                    food = food,
                    navController = navController,
                    isFullScreen = true,
                    favoriteFoods = favoriteFoods,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }

        composable("favorites") {
            FavoriteScreen(
                navController = navController,
                favoriteFoods = favoriteFoods,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavController,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Favorit Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (favoriteFoods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                    Text(
                        text = "Belum ada cemilan favorit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteFoods) { food ->
                    FoodItem(
                        food = food,
                        navController = navController,
                        favoriteFoods = favoriteFoods,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }
        }
    }
}

@Composable
fun DaftarMakananScreen(
    navController: NavController,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit,
    onFoodsLoaded: (List<Cemil>) -> Unit = {}
) {
    var foods by remember { mutableStateOf<List<Cemil>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val repository = remember { FoodRepository() }

    LaunchedEffect(Unit) {
        isLoading = true
        foods = repository.getFoods()
        onFoodsLoaded(foods)
        isLoading = false
        isError = foods.isEmpty()
    }
    var selectedCategory by remember { mutableStateOf("Semua") }

    var searchQuery by remember { mutableStateOf("") }

    val filteredFoods = foods.filter { food ->
        val matchSearch =
            food.nama.contains(searchQuery, ignoreCase = true)

        val matchCategory = when (selectedCategory) {
            "Pedas" ->
                food.nama.contains("pedas", true) ||
                        food.deskripsi.contains("pedas", true)

            "Original" ->
                food.nama.contains("original", true) ||
                        food.deskripsi.contains("original", true)

            "Manis" ->
                food.nama.contains("manis", true) ||
                        food.deskripsi.contains("manis", true)

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
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { 
                                navController.navigate("favorites") {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorit Saya"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        item {
                            FilterChip(
                                selected = selectedCategory == "Semua",
                                onClick = { selectedCategory = "Semua" },
                                label = { Text("Semua") }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedCategory == "Pedas",
                                onClick = { selectedCategory = "Pedas" },
                                label = { Text("Pedas") }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedCategory == "Original",
                                onClick = { selectedCategory = "Original" },
                                label = { Text("Original") }
                            )
                        }

                        item {
                            FilterChip(
                                selected = selectedCategory == "Manis",
                                onClick = { selectedCategory = "Manis" },
                                label = { Text("Manis") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (filteredFoods.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredFoods) { food ->
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Semua Menu",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            if (filteredFoods.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cemilan tidak ditemukan",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                items(filteredFoods) { food ->
                    FoodItem(
                        food = food,
                        navController = navController,
                        favoriteFoods = favoriteFoods,
                        onFavoriteClick = onFavoriteClick
                    )
                }
            }
        }
    }
}

@Composable
fun FoodItem(
    food: Cemil,
    navController: NavController,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit
) {
    DetailScreen(
        food = food,
        navController = navController,
        isFullScreen = false,
        favoriteFoods = favoriteFoods,
        onFavoriteClick = onFavoriteClick
    )
}

@Composable
fun DetailScreen(
    food: Cemil,
    navController: NavController,
    isFullScreen: Boolean = false,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit
) {
    val isFavorite = favoriteFoods.contains(food)
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
                        onClick = {
                            onFavoriteClick(food)
                        },
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ukuran: ${food.ukuran}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
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
                            onClick = {
                                onFavoriteClick(food)
                            },
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Ukuran: ${food.ukuran}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
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
