package com.example.myapplication3.location.calc

data class KmPlusOffset(
    val km: Int? = null,
    val meter: Double,
    val offset: Double = 0.0,
    val crossPoint: Coordinate? = null
) {
    override fun toString(): String = "<KmPlusOffset> {km+: ${km ?: ""}+${String.format("%.1f", meter)}, offset: ${String.format("%.1f", offset)}, cross: ${crossPoint ?: "—"}"
}
