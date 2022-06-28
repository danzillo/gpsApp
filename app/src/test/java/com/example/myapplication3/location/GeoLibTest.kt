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
        // Для сохранения длин до столба
        var lengthToColumn = 0.0
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0
        var previousLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var totalRoadLength = 0.0
        var currentRoadLength = 0.0
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0

        var lengthToColumn2: Double = 0.0

        var projection = 0.0

        val lastCoordinateIndex = axis.lastIndex

        var p = 0.0
        var ploshyad = 0.0
        var height = 0.0
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
            currentRoadLength = Geodesic.WGS84.Inverse(
                axis[counter].latitude,
                axis[counter].longitude,
                axis[counter + 1].latitude,
                axis[counter + 1].longitude
            ).s12
            totalRoadLength += currentRoadLength

            /* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) */
            if (lengthToColumn < minLengthToColumn) {
                minLengthToColumn = lengthToColumn

                // Длина текущего отрезка дороги
                nextLengthToColumn = Geodesic.WGS84.Inverse(
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude,
                    distanceMarks[0].latitude,
                    distanceMarks[0].longitude
                ).s12

                // Длина до столба от следующей вершины
                nextSectionRoadLength = currentRoadLength

                if (counter > 0) {
                    // Длина предыдущего отрезка
                    previousSectionRoadLength = Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        axis[counter - 1].latitude,
                        axis[counter - 1].longitude
                    ).s12

                    // Длина до столба от предыдущей вершины
                    previousLengthToColumn =
                        Geodesic.WGS84.Inverse(
                            axis[counter - 1].latitude,
                            axis[counter - 1].longitude,
                            distanceMarks[0].latitude,
                            distanceMarks[0].longitude
                        ).s12
                }
                // Записываем длину дороги до вершины около столба
                lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength
            }
        }


        /* Зная длину до столба, длину участка дороги между точками
           можно найти cos.
           косинус * | длДоСтолба | = длине проекции */
        if (cos(nextSectionRoadLength / minLengthToColumn) > 0.0
            && minLengthToColumn * cos(previousSectionRoadLength / minLengthToColumn) != 0.0
        ) {
            projection = findSquare(
                nextLengthToColumn,
                minLengthToColumn,
                nextSectionRoadLength
            )
            println("lengthOfRoad = ${lengthOfRoadToColumnVertex + projection}")

        } else {
            projection = findSquare(
                previousLengthToColumn,
                minLengthToColumn,
                previousSectionRoadLength,
            )
            println("lengthOfRoad = ${lengthOfRoadToColumnVertex - projection}")
        }

        println(" AC minLength = $minLengthToColumn")
        println(" AB lengthOfRoad = $previousSectionRoadLength")
        println("lengthOfRoad = $lengthOfRoadToColumnVertex")
        println("projection = $projection")

       // assertEquals(90, projection)
    }

    // Функция для нахождения длины проекции
    private fun findSquare(
        length: Double,
        lengthMin: Double,
        lengthRoad: Double,

    ): Double {
        val p = (length + lengthMin + lengthRoad) / 2
        val square = (p * (p - length) * (p - lengthMin) * (p - lengthRoad)).pow(0.5)
        val height = 2 * square / lengthRoad
        return  (lengthMin.pow(2) - height.pow(2)).pow(0.5)
    }

}