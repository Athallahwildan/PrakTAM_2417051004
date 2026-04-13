package com.example.praktam_2417051004

import com.example.praktam_2417051004.model.Cemil
import com.example.praktam_2417051004.model.FoodSource
import com.example.praktam_2417051004.ui.theme.PrakTAM_2417051004Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakTAM_2417051004Theme{
                DaftarMakananScreen()
            }
        }
    }
}

@Composable
fun DaftarMakananScreen() {
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
                    style = MaterialTheme.typography.titleLarge,
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
                    items(FoodSource.dummyFood) { food ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .height(160.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = food.imageRes),
                                    contentDescription = food.nama,
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
                                        color = MaterialTheme.colorScheme.secondary
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

        items(FoodSource.dummyFood) { food ->
            DetailScreen(food)
        }
    }
}

@Composable
fun DetailScreen(food: Cemil) {
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Box(modifier = Modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(4.dp, Color.Gray)
            .padding(16.dp)

    ) {
        Row {
            Box {
                Image(
                    painter = painterResource(id = food.imageRes),
                    contentDescription = food.nama,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(0.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite Icon",
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

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    delay(2000)

                    snackbarHostState.showSnackbar(
                        "Pesanan ${food.nama} berhasil diproses!"
                    )

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Memproses...")
            } else {
                Text("Pesan Sekarang")
            }
        }
    }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

}

@Preview(showBackground = true)
@Composable
fun DaftarMakananPreview(){
    PrakTAM_2417051004Theme{
        DaftarMakananScreen()
    }
}