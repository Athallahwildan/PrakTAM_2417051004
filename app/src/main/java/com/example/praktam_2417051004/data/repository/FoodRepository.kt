package com.example.praktam_2417051004.data.repository
import com.example.praktam_2417051004.data.api.RetrofitClient
import com.example.praktam_2417051004.data.model.Cemil

class FoodRepository {
    suspend fun getFoods(): List<Cemil> {
        return try {
            RetrofitClient.instance.getFoods()
        } catch (e : Exception) {
            emptyList()
        }
    }
}