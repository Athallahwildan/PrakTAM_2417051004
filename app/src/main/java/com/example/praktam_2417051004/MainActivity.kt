package com.example.praktam_2417051004

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.praktam_2417051004.ui.navigation.AppNavigation
import com.example.praktam_2417051004.ui.theme.PrakTAM_2417051004Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrakTAM_2417051004Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
