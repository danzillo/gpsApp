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
        var lengthOfRoad = 0.0
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
    fun testGeoLibPoints() {
        geoLibCrossPoint(axis, distanceMarks, 0)
        geoLibCrossPoint(axis, distanceMarks, 1)
        geoLibCrossPoint(axis, distanceMarks, 2)
    }

    // Для поиска расстояния от начала координат до проекции столбов
    fun geoLibCrossPoint(
        axis: MutableList<Coordinate>,
        distanceMarks: MutableList<Coordinate>,
        x: Int
    ) {
        // Для сохранения длин до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0
        var previousLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var totalRoadLength = 0.0
        var currentRoadLength: Double
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0
        val cosine: Double

        val projection: Double

        val lastCoordinateIndex = axis.lastIndex

        // Проходимся по всем вершинам
        for (counter in 0 until lastCoordinateIndex - 1) {

            // Находим расстояние от каждой вершины до столба
            lengthToColumn =
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    distanceMarks[x].latitude,
                    distanceMarks[x].longitude
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
                    distanceMarks[x].latitude,
                    distanceMarks[x].longitude
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
                            distanceMarks[x].latitude,
                            distanceMarks[x].longitude
                        ).s12
                }
                // Записываем длину дороги до вершины около столба
                lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength
            }
        }

        cosine = ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
            2)) / 2 * minLengthToColumn * nextSectionRoadLength)

        if (cosine > 0.0) {
            projection = findSquare(
                nextLengthToColumn,
                minLengthToColumn,
                nextSectionRoadLength
            )
            println("Длина до проекции = ${lengthOfRoadToColumnVertex + projection} м")

        } else {
            projection = findSquare(
                previousLengthToColumn,
                minLengthToColumn,
                previousSectionRoadLength,
            )
            println("Длина до проекции = ${lengthOfRoadToColumnVertex - projection} м")
        }

        println("Минимальное расстояние до столба = $minLengthToColumn м")
        println("Расстояние от второй вершины = $nextLengthToColumn м")
        println("Расстояние от второй вершины (сзади)= $previousLengthToColumn м")
        println("Длина предыдущего отрезка дороги = $previousSectionRoadLength м")
        println("Длина следующего отрезка дороги = $nextSectionRoadLength м")
        println("Длина дороги до вершины = $lengthOfRoadToColumnVertex м")
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
        return (lengthMin.pow(2) - height.pow(2)).pow(0.5)
    }

}