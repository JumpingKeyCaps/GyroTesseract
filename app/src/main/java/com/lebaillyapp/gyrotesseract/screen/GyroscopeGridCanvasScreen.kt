package com.lebaillyapp.gyrotesseract.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lebaillyapp.gyrotesseract.composition.GyroscopeDragTesseract

/**
 * GyroTesseract – GyroscopeGridCanvasScreen
 *
 * Hosts the interactive canvas responsible for rendering the 4D tesseract projection.
 * Provides a toggleable input mode: gyroscope-based orientation or manual drag gestures.
 *
 * Composable screen displaying the tesseract canvas.
 *
 * Allows user to toggle between:
 * - **Gyroscope mode** (device orientation controls rotation)
 * - **Drag mode** (manual interaction via touch gestures)
 *
 * The actual 4D rendering and interaction logic is handled by [GyroscopeDragTesseract].
 *
 * @see GyroscopeDragTesseract
 */
@Composable
fun GyroscopeGridCanvasScreen() {
    // Mode selection: Gyroscope or Drag
    var isGyroscopeMode by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        GyroscopeDragTesseract(isGyroscopeMode)

        // UI toggle to switch between modes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 46.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            Switch(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = isGyroscopeMode,
                onCheckedChange = { isGyroscopeMode = it }
            )
        }
    }
}