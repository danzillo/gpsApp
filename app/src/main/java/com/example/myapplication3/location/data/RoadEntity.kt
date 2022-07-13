package com.example.myapplication3.location.data

import com.example.myapplication3.location.kmpluscalc.data.Coordinate
import com.example.myapplication3.location.kmpluscalc.data.KmPlus
import java.util.*

data class RoadEntity(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val startKmDouble: Double = 0.0,
    val startKmPlus: KmPlus = KmPlus(0, 0.0),
    val axis: Array<Coordinate>,
    val distanceMarks: Map<Int, Coordinate>
) {

    override fun toString(): String = "<RoadEntity> {name: \"$name\", startKmDouble: $startKmDouble, "+
            "startKmPlus: $startKmPlus, axis[${axis.size} points], distanceMarks[${distanceMarks.size} points]}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoadEntity

        if (name != other.name) return false
        if (startKmDouble != other.startKmDouble) return false
        if (startKmPlus != other.startKmPlus) return false
        if (!axis.contentEquals(other.axis)) return false
        if (distanceMarks != other.distanceMarks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + startKmDouble.hashCode()
        result = 31 * result + startKmPlus.hashCode()
        result = 31 * result + axis.contentHashCode()
        result = 31 * result + distanceMarks.hashCode()
        return result
    }

}