package com.example.myapplication3.location.kmpluscalc.data

data class KmDoubleOffset(
    val km: Double = 0.0,
    val offset: Double = 0.0,
    val crossPoint: Coordinate? = null
) {
    override fun toString(): String = "<KmDoubleOffset> {km,mmm: ${String.format("%.3f", km)}, offset: ${String.format("%.1f", offset)}, cross: ${crossPoint ?: "—"}"
}

