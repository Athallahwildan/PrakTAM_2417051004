package com.example.praktam_2417051004

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.example.praktam_2417051004.data.model.OrderHistory
import com.example.praktam_2417051004.ui.theme.PrakTAM_2417051004Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.praktam_2417051004.data.repository.FoodRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("food_prefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    var foods by remember { mutableStateOf<List<Cemil>>(emptyList()) }
    var favoriteFoods by remember { mutableStateOf<List<Cemil>>(emptyList()) }
    var cartFoods by remember { mutableStateOf<List<Cemil>>(emptyList()) }
    var selectedCartItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var orderHistory by remember { mutableStateOf<List<OrderHistory>>(emptyList()) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        val favJson = sharedPreferences.getString("favorites", null)
        val cartJson = sharedPreferences.getString("cart", null)
        val historyJson = sharedPreferences.getString("history", null)
        
        if (favJson != null) {
            val type = object : TypeToken<List<Cemil>>() {}.type
            favoriteFoods = gson.fromJson(favJson, type)
        }
        if (cartJson != null) {
            val type = object : TypeToken<List<Cemil>>() {}.type
            cartFoods = gson.fromJson(cartJson, type)
            selectedCartItems = cartFoods.map { it.nama }.toSet()
        }
        if (historyJson != null) {
            val type = object : TypeToken<List<OrderHistory>>() {}.type
            orderHistory = gson.fromJson(historyJson, type)
        }
    }

    LaunchedEffect(favoriteFoods) {
        sharedPreferences.edit().putString("favorites", gson.toJson(favoriteFoods)).apply()
    }
    LaunchedEffect(cartFoods) {
        sharedPreferences.edit().putString("cart", gson.toJson(cartFoods)).apply()
    }
    LaunchedEffect(orderHistory) {
        sharedPreferences.edit().putString("history", gson.toJson(orderHistory)).apply()
    }

    val onFavoriteClick: (Cemil) -> Unit = { selectedFood ->
        favoriteFoods = if (favoriteFoods.any { it.nama == selectedFood.nama }) {
            favoriteFoods.filterNot { it.nama == selectedFood.nama }
        } else {
            favoriteFoods + selectedFood
        }
    }

    val onCartClick: (Cemil) -> Unit = { selectedFood ->
        cartFoods = cartFoods + selectedFood
        selectedCartItems = selectedCartItems + selectedFood.nama
        coroutineScope.launch {
            snackbarHostState.showSnackbar("${selectedFood.nama} berhasil ditambah ke keranjang")
        }
    }

    val onIncreaseQuantity: (Cemil) -> Unit = { food ->
        cartFoods = cartFoods + food
    }

    val onDecreaseQuantity: (Cemil) -> Unit = { food ->

        val mutableCart = cartFoods.toMutableList()

        val index =
            mutableCart.indexOfLast { it.nama == food.nama }

        if (index != -1) {
            mutableCart.removeAt(index)
        }

        cartFoods = mutableCart
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController as androidx.navigation.NavHostController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                DaftarMakananScreen(
                    navController = navController,
                    favoriteFoods = favoriteFoods,
                    cartFoods = cartFoods,
                    onFavoriteClick = onFavoriteClick,
                    onCartClick = onCartClick,
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
                        onFavoriteClick = onFavoriteClick,
                        onCartClick = onCartClick
                    )
                }
            }

            composable("favorites") {
                FavoriteScreen(
                    navController = navController,
                    favoriteFoods = favoriteFoods,
                    onFavoriteClick = onFavoriteClick,
                    onCartClick = onCartClick
                )
            }

            composable("cart") {
                CartScreen(
                    navController = navController,
                    cartFoods = cartFoods,
                    selectedItems = selectedCartItems,
                    onToggleSelect = { nama ->
                        selectedCartItems = if (selectedCartItems.contains(nama)) {
                            selectedCartItems - nama
                        } else {
                            selectedCartItems + nama
                        }
                    },
                    onDelete = { nama ->
                        cartFoods = cartFoods.filterNot { it.nama == nama }
                        selectedCartItems = selectedCartItems - nama
                    },
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity
                )
            }

            composable("checkout/{total}") { backStackEntry ->
                val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
                CheckoutScreen(
                    navController = navController,
                    totalHarga = total,
                    onOrderSuccess = { namaPembeli, totalHarga ->
                        val newOrder = OrderHistory(
                            id = "#${orderHistory.size + 1}",
                            namaPembeli = namaPembeli,
                            totalHarga = totalHarga,
                            tanggal = java.text.SimpleDateFormat(
                                "dd/MM/yyyy",
                                java.util.Locale.getDefault()
                            ).format(java.util.Date())
                        )
                        orderHistory = orderHistory + newOrder
                        cartFoods = cartFoods.filterNot { selectedCartItems.contains(it.nama) }
                        selectedCartItems = emptySet()
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    navController = navController,
                    orders = orderHistory
                )
            }
        }
    }
}

@Composable
fun CustomTopAppBar(title: String, onBack: (() -> Unit)? = null) {
    Surface(shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = if (onBack != null) TextAlign.Center else TextAlign.Start
            )
            if (onBack != null) {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun FavoriteScreen(
    navController: NavController,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit,
    onCartClick: (Cemil) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Favorit Saya", onBack = { navController.popBackStack() })
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
                        onFavoriteClick = onFavoriteClick,
                        onCartClick = onCartClick
                    )
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    navController: NavController,
    cartFoods: List<Cemil>,
    selectedItems: Set<String>,
    onToggleSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onIncreaseQuantity: (Cemil) -> Unit,
    onDecreaseQuantity: (Cemil) -> Unit
) {
    val totalHarga = cartFoods.filter { selectedItems.contains(it.nama) }.sumOf { it.harga }

    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Keranjang Saya", onBack = { navController.popBackStack() })
        }
    ) { paddingValues ->
        if (cartFoods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Keranjang masih kosong",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val groupedFoods = cartFoods.groupBy { it.nama }

                items(groupedFoods.toList()) { (nama, foods) ->
                    val food = foods.first()
                    val quantity = foods.size
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedItems.contains(nama),
                                onCheckedChange = { onToggleSelect(nama) }
                            )

                            AsyncImage(
                                model = food.imageUrl,
                                contentDescription = food.nama,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.basreng),
                                error = painterResource(id = R.drawable.basreng)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = food.nama, fontWeight = FontWeight.Bold)

                                Text(
                                    text = "$quantity x Rp ${food.harga}",
                                    color = MaterialTheme.colorScheme.primary)

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedIconButton(
                                        onClick = { onDecreaseQuantity(food) },
                                        modifier = Modifier.size(32.dp),
                                        enabled = quantity > 1
                                    ) {
                                        Text("-")
                                    }

                                    Text(
                                        text = quantity.toString(),
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                    OutlinedIconButton(
                                        onClick = { onIncreaseQuantity(food) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("+")
                                    }
                                }
                            }
                            
                            IconButton(onClick = { onDelete(nama) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Pembayaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rp $totalHarga",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            navController.navigate("checkout/$totalHarga")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Text("Checkout Sekarang (${selectedItems.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutScreen(
    navController: NavController,
    totalHarga: Int,
    onOrderSuccess: (String, Int) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Checkout", onBack = { navController.popBackStack() })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Ringkasan Pembayaran", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Harga")
                        Text(
                            text = "Rp $totalHarga",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Informasi Pengiriman", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Pembeli") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = alamat,
                onValueChange = { alamat = it },
                label = { Text("Alamat Pengiriman") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noHp,
                onValueChange = { noHp = it },
                label = { Text("Nomor HP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showSuccess = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = nama.isNotBlank() && alamat.isNotBlank() && noHp.isNotBlank()
            ) {
                Text("Konfirmasi Pesanan")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Pesanan Berhasil") },
            text = {
                Text("Terima kasih $nama,\nPesanan Anda senilai Rp $totalHarga sedang diproses 🎉")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOrderSuccess(nama, totalHarga)
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CustomFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DaftarMakananScreen(
    navController: NavController,
    favoriteFoods: List<Cemil>,
    cartFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit,
    onCartClick: (Cemil) -> Unit,
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

                        IconButton(
                            onClick = { 
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Riwayat Pesanan"
                            )
                        }

                        IconButton(
                            onClick = { navController.navigate("cart") }
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Keranjang"
                                )
                                val uniqueProductCount = cartFoods.distinctBy { it.nama }.size
                                if (uniqueProductCount > 0) {
                                    Surface(
                                        color = Color.Red,
                                        shape = CircleShape,
                                        modifier = Modifier.size(16.dp).offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text(
                                            text = "$uniqueProductCount",
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        item {
                            CustomFilterChip(
                                selected = selectedCategory == "Semua",
                                onClick = { selectedCategory = "Semua" },
                                label = "Semua"
                            )
                        }

                        item {
                            CustomFilterChip(
                                selected = selectedCategory == "Pedas",
                                onClick = { selectedCategory = "Pedas" },
                                label = "Pedas"
                            )
                        }

                        item {
                            CustomFilterChip(
                                selected = selectedCategory == "Original",
                                onClick = { selectedCategory = "Original" },
                                label = "Original"
                            )
                        }

                        item {
                            CustomFilterChip(
                                selected = selectedCategory == "Manis",
                                onClick = { selectedCategory = "Manis" },
                                label = "Manis"
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
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1
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
                        onFavoriteClick = onFavoriteClick,
                        onCartClick = onCartClick
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
    onFavoriteClick: (Cemil) -> Unit,
    onCartClick: (Cemil) -> Unit
) {
    DetailScreen(
        food = food,
        navController = navController,
        isFullScreen = false,
        favoriteFoods = favoriteFoods,
        onFavoriteClick = onFavoriteClick,
        onCartClick = onCartClick
    )
}

@Composable
fun DetailScreen(
    food: Cemil,
    navController: NavController,
    isFullScreen: Boolean = false,
    favoriteFoods: List<Cemil>,
    onFavoriteClick: (Cemil) -> Unit,
    onCartClick: (Cemil) -> Unit
) {
    val isFavorite = favoriteFoods.contains(food)
    val snackbarHostState = remember { SnackbarHostState() }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        IconButton(onClick = { onFavoriteClick(food) }) {
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
                    Text(
                        text = food.nama,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = food.deskripsi,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ukuran: ${food.ukuran}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rp ${food.harga}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
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
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { onFavoriteClick(food) },
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
                        Text(
                            text = food.nama,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = food.deskripsi,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Ukuran: ${food.ukuran}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rp ${food.harga}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isFullScreen) 16.dp else 0.dp),
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
                    onClick = { onCartClick(food) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Tambah ke Keranjang")
                }
            }
        }

        if (isFullScreen) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun HistoryScreen(
    navController: NavController,
    orders: List<OrderHistory>
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Riwayat Pesanan",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    ) { paddingValues ->

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada riwayat pesanan")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders.reversed()) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = order.id,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = order.tanggal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Pembeli: ${order.namaPembeli}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Total Pembayaran:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "Rp ${order.totalHarga}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
