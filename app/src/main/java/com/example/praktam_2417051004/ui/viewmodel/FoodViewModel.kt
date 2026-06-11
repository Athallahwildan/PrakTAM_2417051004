package com.example.praktam_2417051004.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.praktam_2417051004.data.model.Cemil
import com.example.praktam_2417051004.data.model.OrderHistory
import com.example.praktam_2417051004.data.repository.FoodRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository()
    private val sharedPreferences = application.getSharedPreferences("food_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var foods by mutableStateOf<List<Cemil>>(emptyList())
        private set

    var favoriteFoods by mutableStateOf<List<Cemil>>(emptyList())
        private set

    var cartFoods by mutableStateOf<List<Cemil>>(emptyList())
        private set

    var selectedCartItems by mutableStateOf<Set<String>>(emptySet())
        private set

    var orderHistory by mutableStateOf<List<OrderHistory>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isError by mutableStateOf(false)
        private set

    init {
        loadFromPrefs()
        fetchFoods()
    }

    fun fetchFoods() {
        viewModelScope.launch {
            isLoading = true
            isError = false
            try {
                val fetchedFoods = repository.getFoods()
                foods = fetchedFoods
                isError = fetchedFoods.isEmpty()
            } catch (e: Exception) {
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadFromPrefs() {
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

    private fun saveToPrefs() {
        sharedPreferences.edit().apply {
            putString("favorites", gson.toJson(favoriteFoods))
            putString("cart", gson.toJson(cartFoods))
            putString("history", gson.toJson(orderHistory))
            apply()
        }
    }

    fun toggleFavorite(food: Cemil) {
        favoriteFoods = if (favoriteFoods.any { it.nama == food.nama }) {
            favoriteFoods.filterNot { it.nama == food.nama }
        } else {
            favoriteFoods + food
        }
        saveToPrefs()
    }

    fun addToCart(food: Cemil) {
        cartFoods = cartFoods + food
        selectedCartItems = selectedCartItems + food.nama
        saveToPrefs()
    }

    fun increaseQuantity(food: Cemil) {
        cartFoods = cartFoods + food
        saveToPrefs()
    }

    fun decreaseQuantity(food: Cemil) {
        val mutableCart = cartFoods.toMutableList()
        val index = mutableCart.indexOfLast { it.nama == food.nama }
        if (index != -1) {
            mutableCart.removeAt(index)
        }
        cartFoods = mutableCart
        saveToPrefs()
    }

    fun toggleCartSelection(nama: String) {
        selectedCartItems = if (selectedCartItems.contains(nama)) {
            selectedCartItems - nama
        } else {
            selectedCartItems + nama
        }
    }

    fun deleteFromCart(nama: String) {
        cartFoods = cartFoods.filterNot { it.nama == nama }
        selectedCartItems = selectedCartItems - nama
        saveToPrefs()
    }

    fun deleteOrder(orderId: String) {
        orderHistory = orderHistory.filterNot { it.id == orderId }
        saveToPrefs()
    }

    fun checkout(namaPembeli: String, totalHarga: Int) {
        val selectedFoods = cartFoods.filter { selectedCartItems.contains(it.nama) }
        val newOrder = OrderHistory(
            id = UUID.randomUUID().toString(),
            namaPembeli = namaPembeli,
            totalHarga = totalHarga,
            tanggal = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            items = selectedFoods
        )
        orderHistory = orderHistory + newOrder
        cartFoods = cartFoods.filterNot { selectedCartItems.contains(it.nama) }
        selectedCartItems = emptySet()
        saveToPrefs()
    }
}
