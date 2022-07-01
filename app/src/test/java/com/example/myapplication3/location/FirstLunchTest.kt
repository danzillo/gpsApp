package com.example.myapplication3.location

import net.sf.geographiclib.Geodesic
import kotlin.math.pow

class FirstLunchTest {
    var lengthToColumn =  Double.MAX_VALUE
    val mapMassiv: MutableMap<Int,Int> = TODO()
    val lengthColumVert: Double = Double.MAX_VALUE

    fun saveVertexColumn() {
        // Ключ - значение для вершины i будем записывать номер столба
        for (markCounter in 0 until distanceMarks.lastIndex) {
            lengthToColumn = Double.MAX_VALUE

            for (counter in 0 until axis.lastIndex) {
                lengthToColumn +=
                    Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        distanceMarks[markCounter].latitude,
                        distanceMarks[markCounter].longitude
                    ).s12


            }
        }


    }
}