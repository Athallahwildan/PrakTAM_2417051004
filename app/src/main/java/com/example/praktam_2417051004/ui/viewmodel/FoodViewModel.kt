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
    private var currentUsername: String = ""

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
        fetchFoods()
    }

    fun setUser(username: String) {
        if (currentUsername != username) {
            currentUsername = username
            loadFromPrefs()
        }
    }

    fun migrateUserData(oldUsername: String, newUsername: String) {
        val favJson = sharedPreferences.getString("favorites_$oldUsername", null)
        val cartJson = sharedPreferences.getString("cart_$oldUsername", null)
        val historyJson = sharedPreferences.getString("history_$oldUsername", null)

        sharedPreferences.edit().apply {
            if (favJson != null) putString("favorites_$newUsername", favJson)
            if (cartJson != null) putString("cart_$newUsername", cartJson)
            if (historyJson != null) putString("history_$newUsername", historyJson)
            
            remove("favorites_$oldUsername")
            remove("cart_$oldUsername")
            remove("history_$oldUsername")
            apply()
        }
        currentUsername = newUsername
    }

    fun clearUserData() {
        currentUsername = ""
        favoriteFoods = emptyList()
        cartFoods = emptyList()
        selectedCartItems = emptySet()
        orderHistory = emptyList()
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
        if (currentUsername.isEmpty()) return

        val favJson = sharedPreferences.getString("favorites_$currentUsername", null)
        val cartJson = sharedPreferences.getString("cart_$currentUsername", null)
        val historyJson = sharedPreferences.getString("history_$currentUsername", null)

        val foodListType = object : TypeToken<List<Cemil>>() {}.type
        val historyListType = object : TypeToken<List<OrderHistory>>() {}.type

        favoriteFoods = if (favJson != null) gson.fromJson(favJson, foodListType) else emptyList()
        cartFoods = if (cartJson != null) gson.fromJson(cartJson, foodListType) else emptyList()
        selectedCartItems = cartFoods.map { it.nama }.toSet()
        orderHistory = if (historyJson != null) gson.fromJson(historyJson, historyListType) else emptyList()
    }

    private fun saveToPrefs() {
        if (currentUsername.isEmpty()) return

        sharedPreferences.edit().apply {
            putString("favorites_$currentUsername", gson.toJson(favoriteFoods))
            putString("cart_$currentUsername", gson.toJson(cartFoods))
            putString("history_$currentUsername", gson.toJson(orderHistory))
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
