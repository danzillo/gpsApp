package com.example.myapplication3.location

import org.junit.Test
import net.sf.geographiclib.*
import org.junit.Assert.assertEquals
import kotlin.math.cos
import kotlin.math.pow

internal class GeoLibTest {

    // Тестирование с помощью библиотеки
    @Test
    fun geoLibLength() {
        var lengthOfRoad: Double = 0.0
        val lastCoordinateIndex = axis.lastIndex
        var counter = 0
        while (counter < lastCoordinateIndex) {
            lengthOfRoad +=
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12
            counter += 1
        }
        assertEquals(3625.184, lengthOfRoad, 0.01)
    }

    @Test
    fun geoLibCrossPoint() {
        var lengthToColumn: Double = 0.0
        var minLengthToColumn: Double = Double.MAX_VALUE

        var totalRoadLength = 0.0
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0

        var lengthToColumn2: Double = 0.0

        var projection = 0.0

        val lastCoordinateIndex = axis.lastIndex

        // Проходимся по всем вершинам
        for (counter in 0 until lastCoordinateIndex - 1) {

            // Находим расстояние от каждой вершины до столба
            lengthToColumn =
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    distanceMarks[0].latitude,
                    distanceMarks[0].longitude
                ).s12

            // Считаем длину суммарную длину дороги между вершинами
            totalRoadLength += Geodesic.WGS84.Inverse(
                axis[counter].latitude,
                axis[counter].longitude,
                axis[counter + 1].latitude,
                axis[counter + 1].longitude
            ).s12

            /* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) */
            if (lengthToColumn < minLengthToColumn) {
                minLengthToColumn = lengthToColumn
                nextSectionRoadLength = Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12

                if (counter > 0) {
                    previousSectionRoadLength = Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        axis[counter - 1].latitude,
                        axis[counter - 1].longitude
                    ).s12
                    // Длина до столба от предыдущей вершины
                    lengthToColumn2 =
                        Geodesic.WGS84.Inverse(
                            axis[counter-1].latitude,
                            axis[counter-1].longitude,
                            distanceMarks[0].latitude,
                            distanceMarks[0].longitude
                        ).s12
                }
                // Записываем длину дороги до вершины около столба
                lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength
            }
        }
        val p = (lengthToColumn2+minLengthToColumn+previousSectionRoadLength)/2
        val ploshyad = (p*(p-lengthToColumn2)*(p-minLengthToColumn)*(p-previousSectionRoadLength)).pow(0.5)
        val hier = 2*ploshyad/previousSectionRoadLength

        /* Зная длину до столба, длину участка дороги между точками
           можно найти cos.
           косинус * | длДоСтолба | = длине проекции */
        projection =
            if (minLengthToColumn * cos(nextSectionRoadLength / minLengthToColumn) > 0.0
                && minLengthToColumn * cos(previousSectionRoadLength / minLengthToColumn) != 0.0
            )
                minLengthToColumn * cos(nextSectionRoadLength / minLengthToColumn)
            else minLengthToColumn * cos(previousSectionRoadLength / minLengthToColumn)

        println(" AC minLength = $minLengthToColumn")
        println(" AB lengthOfRoad = $previousSectionRoadLength")
        println( (minLengthToColumn.pow(2) - hier.pow(2)).pow(0.5))
        println("lengthOfRoad = $lengthOfRoadToColumnVertex")
        println("projection = $projection")
        println("(projection + lengthOfRoad) = ${ lengthOfRoadToColumnVertex - (minLengthToColumn.pow(2) - hier.pow(2)).pow(0.5)}")


        assertEquals(90, projection + lengthOfRoadToColumnVertex)
    }
}