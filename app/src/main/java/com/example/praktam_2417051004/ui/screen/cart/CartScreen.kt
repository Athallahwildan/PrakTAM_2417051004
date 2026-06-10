package com.example.praktam_2417051004.ui.screen.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.praktam_2417051004.ui.components.CustomTopAppBar
import com.example.praktam_2417051004.ui.viewmodel.FoodViewModel

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: FoodViewModel
) {
    val cartFoods = viewModel.cartFoods
    val selectedItems = viewModel.selectedCartItems
    val totalHarga = cartFoods.filter { selectedItems.contains(it.nama) }.sumOf { it.harga }

    Scaffold(
        topBar = {
            CustomTopAppBar(title = "Keranjang Saya", onBack = { navController.popBackStack() })
        }
    ) { paddingValues ->
        if (cartFoods.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Keranjang masih kosong", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedItems.contains(nama),
                                onCheckedChange = { viewModel.toggleCartSelection(nama) }
                            )

                            AsyncImage(
                                model = food.imageUrl,
                                contentDescription = food.nama,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.basreng),
                                error = painterResource(id = R.drawable.basreng)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = food.nama, fontWeight = FontWeight.Bold)
                                Text(text = "$quantity x Rp ${food.harga}", color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedIconButton(
                                        onClick = { viewModel.decreaseQuantity(food) },
                                        modifier = Modifier.size(32.dp),
                                        enabled = quantity > 1
                                    ) { Text("-") }
                                    Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 12.dp))
                                    OutlinedIconButton(
                                        onClick = { viewModel.increaseQuantity(food) },
                                        modifier = Modifier.size(32.dp)
                                    ) { Text("+") }
                                }
                            }
                            
                            IconButton(onClick = { viewModel.deleteFromCart(nama) }) {
                                Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Pembayaran", fontWeight = FontWeight.Bold)
                        Text(text = "Rp $totalHarga", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.navigate("checkout/$totalHarga") },
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
