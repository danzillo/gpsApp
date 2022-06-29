package com.example.myapplication3.location

import org.junit.Test
import net.sf.geographiclib.*
import org.junit.Assert.assertEquals
import kotlin.math.abs
import kotlin.math.pow

internal class GeoLibTest {

    // Тестирование общего расстояния с помощью библиотеки
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
        geoLibKilometersCalc(axis, distanceMarks, 0)
        geoLibKilometersCalc(axis, distanceMarks, 1)
        geoLibKilometersCalc(axis, distanceMarks, 2)
    }

    // Для поиска расстояния от начала координат до проекции столбов
    // получает координаты дороги и конкретный километровый столб
    fun geoLibKilometersCalc(
        axis: MutableList<Coordinate>,
        distanceMarks: MutableList<Coordinate>,
        x: Int
    ) {
        // Для сохранения расстояний до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0
        var previousLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var offset = 0
        var totalLengthOfRoadToColumn = 0.0
        var totalRoadLength = 0.0
        var currentRoadLength: Double
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0

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

        val cosine: Double =
            ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                2
            )) / 2 * minLengthToColumn * nextSectionRoadLength)

        if (cosine >= 0.0 && nextSectionRoadLength != 0.0) {
            projection = findProjectionLength(
                minLengthToColumn,
                findOffset(
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
            )
            totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex + projection
            offset = findOffset(
                nextLengthToColumn,
                minLengthToColumn,
                nextSectionRoadLength,
            ).toInt()

        } else if (cosine < 0.0 && previousSectionRoadLength != 0.0) {
            projection = findProjectionLength(
                minLengthToColumn,
                findOffset(
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
            )
            totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex - projection
            offset = findOffset(
                nextLengthToColumn,
                minLengthToColumn,
                nextSectionRoadLength,
            ).toInt()
        } else println("Невозможно рассчитать КМ+М для данного столба.")

        println(
            "КМ+М: ${convertMeterToKilometer(totalLengthOfRoadToColumn)} +" +
                    " ${
                        abs(
                            (convertMeterToKilometer(totalLengthOfRoadToColumn)) * 1000 -
                                    (totalLengthOfRoadToColumn.toInt())
                        )
                    } " +
                    "\nOffset: $offset\n"
        )
    }

    // Функция для нахождения смещения от дороги
    private fun findOffset(
        length: Double,
        lengthMin: Double,
        lengthRoad: Double,

        ): Double {
        val p = (length + lengthMin + lengthRoad) / 2
        val square = (p * (p - length) * (p - lengthMin) * (p - lengthRoad)).pow(0.5)
        return 2 * square / lengthRoad
    }

    // Функция для нахождения длины проекции
    private fun findProjectionLength(
        lengthMin: Double,
        height: Double,
    ): Double {
        return (lengthMin.pow(2) - height.pow(2)).pow(0.5)
    }

    private fun convertMeterToKilometer(meters: Double): Int {
        return (meters / 1000).toInt()
    }

}