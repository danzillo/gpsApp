package com.example.myapplication3.location.kmpluscalc.data

data class Coordinate(
    val x: Double,
    val y: Double
) {
    override fun toString(): String = "<Coordinate> {x: $x, y: $y}"
}
