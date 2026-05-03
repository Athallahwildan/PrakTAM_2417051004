package com.example.praktam_2417051004.model
import android.content.Context

object FoodSource {
    fun getResourceId(context: Context, imageName: String): Int {
        return context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }
}