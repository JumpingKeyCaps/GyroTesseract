package com.lebaillyapp.gyrotesseract.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * GyroTesseract – MainScreen
 *
 * Top-level composable hosting the core visual experience of the app.
 * This screen simply delegates to [GyroscopeGridCanvasScreen], applying full-size layout constraints.
 *
 * Part of the gyroscope-driven 4D visualization flow.
 *
 * @see GyroscopeGridCanvasScreen
 *
 * MainScreen – Composable entry point for the app UI.
 *
 * - Applies provided [modifier] with full size by default.
 * - Hosts the [GyroscopeGridCanvasScreen] which contains the 4D rendering logic.
 *
 * @param modifier Layout modifier passed by the host activity (e.g. padding for insets).
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        GyroscopeGridCanvasScreen()
    }
}