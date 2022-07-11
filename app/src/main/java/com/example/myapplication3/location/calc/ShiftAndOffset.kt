package com.example.myapplication3.location.calc

import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import kotlin.math.*

data class ShiftAndOffset(
    val shift: Double,
    val offset: Double,
    val crossPoint: Coordinate,
    val prevPoint: Int,
    val nextPoint: Int,
    var minPoint: Int,
    val totalLength: Double,
    val isAheadPoint:Boolean
) {
    override fun toString() =
        "<ShiftAndOffset> {shift: $shift, offset: $offset, lat: ${crossPoint.latitude}, long:  ${crossPoint.longitude}}"
}