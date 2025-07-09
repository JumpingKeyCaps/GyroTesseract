package com.lebaillyapp.gyrotesseract.model

/**
 * Représente un point dans l'espace 4D, avec une coordonnée supplémentaire W.
 *
 * @property x Coordonnée X
 * @property y Coordonnée Y
 * @property z Coordonnée Z
 * @property w Coordonnée W (dimension supplémentaire)
 */
data class Vertex4D(val x: Float, val y: Float, val z: Float, val w: Float)