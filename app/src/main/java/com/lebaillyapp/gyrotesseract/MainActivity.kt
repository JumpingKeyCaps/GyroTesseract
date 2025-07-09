package com.lebaillyapp.gyrotesseract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.lebaillyapp.gyrotesseract.screen.MainScreen
import com.lebaillyapp.gyrotesseract.ui.theme.GyroTesseractTheme

/**
 * MainActivity – Compose activity that sets up the app's main content using a themed Scaffold.
 *
 * - Enables edge-to-edge drawing.
 * - Hosts the main UI logic via [MainScreen].
 * - Applies system window insets through inner padding.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GyroTesseractTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}