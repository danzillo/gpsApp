package com.example.myapplication3.location.kmpluscalc.data

import java.io.Serializable

/**
 * Представление километража КМ+М.
 * Номер километрового столба, плюс метровое смещение вдоль оси дороги
 * по направлению увеличения километража.
 */
data class KmPlus(
    val km: Int? = null,
    val meter: Double
) : Serializable {
    override fun toString(): String = "<KmPlus> {km+: ${km ?: ""}+${String.format("%.1f", meter)}"

    fun memorySize(): Long = 4L + 8L
}
