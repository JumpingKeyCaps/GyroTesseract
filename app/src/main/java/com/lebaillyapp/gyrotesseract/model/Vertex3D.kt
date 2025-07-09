package com.lebaillyapp.gyrotesseract.model

/**
 * Représente un point dans l'espace 3D utilisé après projection 4D→3D.
 *
 * @property x Coordonnée X
 * @property y Coordonnée Y
 * @property z Coordonnée Z
 */
data class Vertex3D(val x: Float, val y: Float, val z: Float)