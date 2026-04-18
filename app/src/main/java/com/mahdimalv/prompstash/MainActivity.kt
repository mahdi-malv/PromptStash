package com.mahdimalv.prompstash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mahdimalv.prompstash.ui.navigation.AppNavigation
import com.mahdimalv.prompstash.ui.theme.PrompStashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrompStashTheme {
                AppNavigation()
            }
        }
    }
}
