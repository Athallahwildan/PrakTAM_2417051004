package com.example.praktam_2417051004.data.repository
import android.util.Log
import com.example.praktam_2417051004.data.api.RetrofitClient
import com.example.praktam_2417051004.data.model.Cemil

class FoodRepository {
    suspend fun getFoods(): List<Cemil> {
        return try {
            val response = RetrofitClient.instance.getFoods()
            Log.d("FoodRepository", "Data loaded: ${response.size} items")
            response
        } catch (e : Exception) {
            emptyList()
        }
    }
}