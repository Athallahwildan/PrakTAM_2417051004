package com.example.praktam_2417051004.network

import com.example.praktam_2417051004.model.Cemil
import retrofit2.http.GET

interface ApiService {
    @GET("menu_makanan.json")
    suspend fun getFoods(): List<Cemil>
}
