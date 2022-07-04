/*
package com.example.myapplication3.location

import net.sf.geographiclib.Geodesic
import org.junit.Test
import kotlin.math.pow

class FirstLunchTest {
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

    @Test
    fun saveVertexColumn() {
        // Для сохранения длин до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var nextSectionRoadLength = 0.0
        var cosine: Double
        var saveCounter = 0.0
        var currentRoadLength: Double = 0.0

        val lastCoordinateIndex = axis.lastIndex

        for (markCounter in 0 until distanceMarks.lastIndex) {
            minLengthToColumn = Double.MAX_VALUE
            println(saveCounter)
            if (saveCounter != 0.0)
                lengthOfColumn.add(saveCounter)
            // saveCounter = 0.0
            // Проходимся по всем вершинам
            for (counter in 0 until lastCoordinateIndex) {

                // Текущая длина дороги
                currentRoadLength = Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12

                // Находим расстояние от каждой вершины до столба
                lengthToColumn =
                    Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        distanceMarks[markCounter].latitude,
                        distanceMarks[markCounter].longitude
                    ).s12

 Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                    участков дороги (от данной вершины до следующей и от данной вершины до предыдущей)

                if (lengthToColumn < minLengthToColumn) {
                    minLengthToColumn = lengthToColumn

                    // Длина до столба от следующей вершины
                    nextLengthToColumn = Geodesic.WGS84.Inverse(
                        axis[counter + 1].latitude,
                        axis[counter + 1].longitude,
                        distanceMarks[markCounter].latitude,
                        distanceMarks[markCounter].longitude
                    ).s12

                    // Длина текущего отрезка дороги
                    nextSectionRoadLength = currentRoadLength
                    cosine =
                        ((minLengthToColumn.pow(2) + currentRoadLength.pow(2) - nextLengthToColumn.pow(
                            2
                        )) / 2 * minLengthToColumn * currentRoadLength)

                    if (cosine > 0.0) {
                        saveCounter = counter.toDouble()
                    } else {
                        saveCounter = counter.toDouble() - 1.0

                    }


                }
            }
        }
        // Сохраняем номера вершин до каждого из столбов в lengthofColumn()
        println(lengthOfColumn.get(0))
    }
}
*/
