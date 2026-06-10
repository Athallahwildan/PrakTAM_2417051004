package com.example.praktam_2417051004.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.praktam_2417051004.data.model.User
import com.google.gson.Gson

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var isLoggedIn by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val userJson = sharedPreferences.getString("user_data", null)
            if (userJson != null) {
                currentUser = gson.fromJson(userJson, User::class.java)
            }
        }
    }

    fun login(username: String, password: String): Boolean {
        // Dummy logic login
        if (username.isNotEmpty() && password.isNotEmpty()) {
            val user = User(
                username = username,
                name = "Pengguna $username",
                email = "$username@email.com"
            )
            currentUser = user
            isLoggedIn = true
            
            sharedPreferences.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_data", gson.toJson(user))
                apply()
            }
            return true
        }
        return false
    }

    fun logout() {
        isLoggedIn = false
        currentUser = null
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", false)
            remove("user_data")
            apply()
        }
    }
}
