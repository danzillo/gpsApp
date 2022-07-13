package com.example.myapplication3.location.kmpluscalc.data

data class KmPlusOffset(
    val km: Int? = null,
    var meter: Double,
    var offset: Double = 0.0,
    val crossPoint: Coordinate? = null
) {
    override fun toString(): String = "<KmPlusOffset> {km+: ${km ?: ""}+${String.format("%.1f", meter)}, offset: ${String.format("%.1f", offset)}, cross: ${crossPoint ?: "—"}"
}
