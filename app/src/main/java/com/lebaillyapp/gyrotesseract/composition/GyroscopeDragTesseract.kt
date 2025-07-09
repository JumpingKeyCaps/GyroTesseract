package com.lebaillyapp.gyrotesseract.composition

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.lebaillyapp.gyrotesseract.model.Vertex3D
import com.lebaillyapp.gyrotesseract.model.Vertex4D
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * GyroTesseract – GyroscopeDragTesseract
 *
 * Interactive 4D tesseract rendering using gyroscope or drag gesture input.
 *
 * This composable draws a projected 4D hypercube (tesseract) on a Canvas, where the user can:
 * - Rotate it via gyroscope sensor (rotation vector)
 * - Manually rotate it via drag with inertial momentum
 *
 * Projection pipeline:
 * - 4D rotation (x-w and y-z planes)
 * - 4D → 3D perspective projection
 * - 3D → 2D perspective projection onto the Canvas
 *
 * In drag mode, rotation inertia and deceleration are handled manually.
 * In gyro mode, rotation vector data is filtered with a low-pass algorithm.
 *
 * @param isGyroscopeMode `true` to enable sensor rotation, `false` for drag gesture mode.
 */
@Composable
fun GyroscopeDragTesseract(isGyroscopeMode: Boolean = false) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SensorManager::class.java)

    // Gyroscope values (angles)
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    // Drag mode rotation variables
    var dragTiltX by remember { mutableStateOf(0f) }
    var dragTiltY by remember { mutableStateOf(0f) }

    // Filtrage passe-bas pour gyroscope
    val alpha = 0.1f
    var filteredTiltX by remember { mutableStateOf(0f) }
    var filteredTiltY by remember { mutableStateOf(0f) }

    // Variables pour l'inertie
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Variables pour mesurer le temps entre les déplacements
    var lastUpdateTime by remember { mutableStateOf(0L) }
    var lastDragX by remember { mutableStateOf(0f) }
    var lastDragY by remember { mutableStateOf(0f) }

    // Variables pour gérer la décélération manuelle
    var isDecelerating by remember { mutableStateOf(false) }
    var inertiaX by remember { mutableStateOf(0f) }
    var inertiaY by remember { mutableStateOf(0f) }

    // Constante de décélération - plus la valeur est petite, plus la décélération est lente
    val decelerationRate = 0.999f  // Valeur proche de 1 pour une décélération très lente

    // Listener pour le gyroscope
    val sensorEventListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    val orientationValues = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    // On utilise les angles d'orientation (en radians)
                    tiltX = orientationValues[1]
                    tiltY = orientationValues[2]
                    // Filtrage passe-bas
                    filteredTiltX = alpha * tiltX + (1 - alpha) * filteredTiltX
                    filteredTiltY = alpha * tiltY + (1 - alpha) * filteredTiltY
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    // Enregistrer/désenregistrer le listener
    DisposableEffect(Unit) {
        val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager?.registerListener(
            sensorEventListener.value,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose {
            sensorManager?.unregisterListener(sensorEventListener.value)
        }
    }

    // Gestion de la décélération linéaire manuelle
    LaunchedEffect(isDecelerating) {
        if (isDecelerating && !isGyroscopeMode) {
            // Initialiser l'inertie avec la vitesse actuelle (réduite pour ralentir)
            inertiaX = velocityX * 0.9f // Réduire la vitesse initiale
            inertiaY = velocityY * 0.9f

            // Boucle de décélération
            while (isDecelerating && (abs(inertiaX) > 0.0001f || abs(inertiaY) > 0.0001f)) {
                // Appliquer l'inertie actuelle à la rotation
                dragTiltX += inertiaX
                dragTiltY += inertiaY

                // Décélération linéaire
                inertiaX *= decelerationRate
                inertiaY *= decelerationRate

                // Arrêter la décélération si les valeurs sont trop petites
                if (abs(inertiaX) < 0.0001f && abs(inertiaY) < 0.0001f) {
                    isDecelerating = false
                }

                // Attendre avant la prochaine frame
                delay(16) // ~60fps
            }
        }
    }

    // Détecter le drag et appliquer la rotation
    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    isDecelerating = false // Arrêter toute décélération en cours
                    lastUpdateTime = System.currentTimeMillis()
                    lastDragX = 0f
                    lastDragY = 0f
                },
                onDragEnd = {
                    isDecelerating = true // Démarrer la décélération
                },
                onDrag = { _, dragAmount ->
                    // Mesurer le temps écoulé depuis le dernier drag pour calculer la vélocité
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = (currentTime - lastUpdateTime).coerceAtLeast(1L)
                    lastUpdateTime = currentTime

                    // Calculer la vélocité (réduite pour une rotation plus lente)
                    velocityX = dragAmount.x * 0.005f / deltaTime // Sensibilité réduite
                    velocityY = dragAmount.y * 0.005f / deltaTime

                    // Lisser la vélocité
                    velocityX = 0.7f * velocityX + 0.3f * lastDragX
                    velocityY = 0.7f * velocityY + 0.3f * lastDragY

                    lastDragX = velocityX
                    lastDragY = velocityY

                    // Ajustement de la sensibilité pour une rotation plus lente
                    dragTiltX += dragAmount.x * 0.005f // Sensibilité réduite pour une rotation plus lente
                    dragTiltY += dragAmount.y * 0.005f
                }
            )
        }) {

        val canvasWidth = size.width
        val canvasHeight = size.height
        val center = Offset(canvasWidth / 2, canvasHeight / 2)

        // Taille de base du tesseract
        val tesseractSize = 100f

        // Générer les 16 sommets du tesseract en 4D
        val vertices4D = mutableListOf<Vertex4D>()
        for (x in listOf(-tesseractSize, tesseractSize)) {
            for (y in listOf(-tesseractSize, tesseractSize)) {
                for (z in listOf(-tesseractSize, tesseractSize)) {
                    for (w in listOf(-tesseractSize, tesseractSize)) {
                        vertices4D.add(Vertex4D(x, y, z, w))
                    }
                }
            }
        }

        // Définir les arêtes : deux sommets sont reliés si leurs coordonnées diffèrent en exactement UNE dimension
        val edges = mutableListOf<Pair<Int, Int>>()
        for (i in vertices4D.indices) {
            for (j in i + 1 until vertices4D.size) {
                if (countDifferences(vertices4D[i], vertices4D[j]) == 1) {
                    edges.add(Pair(i, j))
                }
            }
        }

        // Rotation 4D (sans les animations, uniquement avec le drag et l'inertie)
        val angleXW = if (isGyroscopeMode) filteredTiltX else dragTiltX
        val angleYZ = if (isGyroscopeMode) filteredTiltY else dragTiltY
        val rotated4D = vertices4D.map { vertex ->
            val newX = vertex.x * cos(angleXW) - vertex.w * sin(angleXW)
            val newW = vertex.x * sin(angleXW) + vertex.w * cos(angleXW)
            val tempVertex = Vertex4D(newX, vertex.y, vertex.z, newW)
            val newY = tempVertex.y * cos(angleYZ) - tempVertex.z * sin(angleYZ)
            val newZ = tempVertex.y * sin(angleYZ) + tempVertex.z * cos(angleYZ)
            Vertex4D(tempVertex.x, newY, newZ, tempVertex.w)
        }

        // Projection 4D → 3D
        val d4 = 300f
        val vertices3D = rotated4D.map { vertex ->
            val factor4 = d4 / (d4 - vertex.w)
            Vertex3D(vertex.x * factor4, vertex.y * factor4, vertex.z * factor4)
        }

        // Projection 3D → 2D
        val d3 = 300f
        val projected2D = vertices3D.map { vertex ->
            val factor3 = d3 / (d3 - vertex.z)
            Offset(vertex.x * factor3 + center.x, vertex.y * factor3 + center.y)
        }

        // Dessiner les arêtes du tesseract
        edges.forEach { (i, j) ->
            drawLine(
                color = Color.White,
                start = projected2D[i],
                end = projected2D[j],
                strokeWidth = 6f
            )
        }
    }
}

/**
 * Utility function that counts how many coordinates differ between two 4D vertices.
 *
 * Used to determine edge connectivity for the tesseract.
 *
 * @param v1 First vertex
 * @param v2 Second vertex
 * @return Number of coordinate differences (0 to 4)
 */
fun countDifferences(v1: Vertex4D, v2: Vertex4D): Int =
    listOf(v1.x != v2.x, v1.y != v2.y, v1.z != v2.z, v1.w != v2.w).count { it }