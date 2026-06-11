package com.example.praktam_2417051004.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.praktam_2417051004.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    fun register(name: String, email: String, username: String, password: String): Boolean {
        val users = getRegisteredUsers()
        if (users.containsKey(username)) return false

        users[username] = mapOf("name" to name, "email" to email, "password" to password)
        saveRegisteredUsers(users)
        return true
    }

    fun login(username: String, password: String): Boolean {
        val users = getRegisteredUsers()
        val userData = users[username]
        if (userData != null && userData["password"] == password) {
            saveLoginSession(User(username, userData["name"] ?: "", userData["email"] ?: ""))
            return true
        }
        return false
    }

    fun updateProfile(newName: String, newEmail: String, newUsername: String, newPassword: String? = null): Boolean {
        val oldUser = currentUser ?: return false
        val users = getRegisteredUsers()
        
        // Jika username ganti, cek apakah username baru sudah dipakai orang lain
        if (newUsername != oldUser.username && users.containsKey(newUsername)) return false

        val userData = users[oldUser.username]?.toMutableMap() ?: mutableMapOf()
        userData["name"] = newName
        userData["email"] = newEmail
        if (!newPassword.isNullOrBlank()) {
            userData["password"] = newPassword
        }

        if (newUsername != oldUser.username) {
            users.remove(oldUser.username)
        }
        users[newUsername] = userData
        
        saveRegisteredUsers(users)
        val updatedUser = User(newUsername, newName, newEmail)
        saveLoginSession(updatedUser)
        return true
    }

    fun getPassword(): String {
        val username = currentUser?.username ?: return ""
        return getRegisteredUsers()[username]?.get("password") ?: ""
    }

    private fun getRegisteredUsers(): MutableMap<String, Map<String, String>> {
        val json = sharedPreferences.getString("registered_users", "{}")
        val type = object : TypeToken<MutableMap<String, Map<String, String>>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveRegisteredUsers(users: Map<String, Map<String, String>>) {
        sharedPreferences.edit().putString("registered_users", gson.toJson(users)).apply()
    }

    private fun saveLoginSession(user: User) {
        currentUser = user
        isLoggedIn = true
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_data", gson.toJson(user))
            apply()
        }
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
