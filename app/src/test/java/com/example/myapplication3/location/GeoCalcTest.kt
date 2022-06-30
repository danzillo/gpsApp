package com.example.myapplication3.location

import org.junit.Test
import net.sf.geographiclib.*
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.pow

internal class GeoCalcTest {

    @Test
    fun testGeoLibPoints() {
        geoLibCalc(axis, distanceMarks)
    }


    // Для поиска расстояния от начала координат до проекции столбов
    // получает координаты дороги и конкретный километровый столб

    fun geoLibCalc(
        axis: MutableList<Coordinate>,
        distanceMarks: MutableList<Coordinate>
    ) {
        // Для сохранения расстояний до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double
        var nextLengthToColumn: Double
        var previousLengthToColumn: Double
        var offset: Double
        var projection: Double

        // Для сохранения длин отрезков между вершинами
        var totalLengthOfRoadToColumn: Double
        var totalRoadLength: Double
        var currentRoadLength: Double
        var lengthOfRoadToColumnVertex: Double
        var nextSectionRoadLength: Double
        var previousSectionRoadLength: Double

        // Последние индексы вершин и столбов
        val lastVertexIndex = axis.lastIndex
        val lastColumnIndex = distanceMarks.lastIndex

        // Проходимся по всем вершинам
        for (columnCounter in 0 until lastColumnIndex - 1) {
            // Для сохранения расстояний до столба
            minLengthToColumn = Double.MAX_VALUE
            nextLengthToColumn = 0.0
            previousLengthToColumn = 0.0
            offset = 0.0
            projection = 0.0

            // Для сохранения длин отрезков между вершинами
            totalLengthOfRoadToColumn = 0.0
            totalRoadLength = 0.0
            lengthOfRoadToColumnVertex = 0.0
            nextSectionRoadLength = 0.0
            previousSectionRoadLength = 0.0
            for (vertexCounter in 0 until lastVertexIndex - 1) {

                // Находим расстояние от каждой вершины до столба
                lengthToColumn =
                    Geodesic.WGS84.Inverse(
                        axis[vertexCounter].latitude,
                        axis[vertexCounter].longitude,
                        distanceMarks[columnCounter].latitude,
                        distanceMarks[columnCounter].longitude
                    ).s12

                // Считаем длину суммарную длину дороги между вершинами
                currentRoadLength = Geodesic.WGS84.Inverse(
                    axis[vertexCounter].latitude,
                    axis[vertexCounter].longitude,
                    axis[vertexCounter + 1].latitude,
                    axis[vertexCounter + 1].longitude
                ).s12

                // Считаем общее расстояние дороги
                totalRoadLength += currentRoadLength

                /* Если найденное расстояние меньше того, что было, то сохраняем его, а также
                 расстояние от прошлой/следующей вершины и длину участков дороги
                 (от данной вершины до следующей и от данной вершины до предыдущей) */
                if (lengthToColumn < minLengthToColumn) {

                    // Сохранение текущего состояния
                    minLengthToColumn = lengthToColumn

                    // Длина до столба от следующей вершины
                    nextLengthToColumn = Geodesic.WGS84.Inverse(
                        axis[vertexCounter + 1].latitude,
                        axis[vertexCounter + 1].longitude,
                        distanceMarks[columnCounter].latitude,
                        distanceMarks[columnCounter].longitude
                    ).s12

                    // Длина текущего отрезка дороги
                    nextSectionRoadLength = currentRoadLength

                    if (vertexCounter > 0) {

                        // Длина предыдущего отрезка
                        previousSectionRoadLength = Geodesic.WGS84.Inverse(
                            axis[vertexCounter].latitude,
                            axis[vertexCounter].longitude,
                            axis[vertexCounter - 1].latitude,
                            axis[vertexCounter - 1].longitude
                        ).s12

                        // Длина до столба от предыдущей вершины
                        previousLengthToColumn =
                            Geodesic.WGS84.Inverse(
                                axis[vertexCounter - 1].latitude,
                                axis[vertexCounter - 1].longitude,
                                distanceMarks[columnCounter].latitude,
                                distanceMarks[columnCounter].longitude
                            ).s12
                    }

                    // Записываем длину дороги до вершины около столба
                    lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength
                }
            }

            // Косинус угла между столбом и вершиной дороги для определения того,
            // с какой стороны от столба находится вершина
            val cousins: Double =
                ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                    2
                )) / 2 * minLengthToColumn * nextSectionRoadLength)

            // Если cos > 0 ==> вначале вершина, затем столб
            if (cousins >= 0.0 && nextSectionRoadLength != 0.0) {

                // Находим длину проекции
                projection = findProjectionLength(
                    minLengthToColumn,
                    findOffset(
                        nextLengthToColumn,
                        minLengthToColumn,
                        nextSectionRoadLength,
                    )
                )

                // Считаем суммарную длину от начала до проекции столба
                totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex + projection

                // Смещение столба относительно оси дороги (сделать toInt)
                offset = findOffset(
                    nextLengthToColumn,
                    minLengthToColumn,
                    nextSectionRoadLength,
                )

                // Если cos < 0 ==> вначале столб, затем вершина
            } else if (cousins < 0.0 && previousSectionRoadLength != 0.0) {

                // Находим длину проекции
                projection = findProjectionLength(
                    minLengthToColumn,
                    findOffset(
                        previousLengthToColumn,
                        minLengthToColumn,
                        previousSectionRoadLength,
                    )
                )

                // Считаем суммарную длину от начала до проекции столба
                totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex - projection

                // Смещение столба относительно оси дороги (сделать toInt)
                offset = findOffset(
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
            } else println("Невозможно рассчитать КМ+М для данного столба.")
            println(
                "КМ+М: ${convertMeterToKilometer(totalLengthOfRoadToColumn)} +" +
                        " ${
                            abs(
                                (convertMeterToKilometer(totalLengthOfRoadToColumn)) * 1000 -
                                        (totalLengthOfRoadToColumn.toInt())
                            )
                        } " +
                        "\nOffset: ${offset.toInt()}\n"
            )
        }


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
/*       println(
            "Высота: ${
                findOffset(
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
            }"
        )
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
        println(
            " ${
                Geodesic.WGS84.Direct(
                    distanceMarks[1].latitude,
                    distanceMarks[1].longitude,
                    asin(projection / minLengthToColumn),
                    offset
                ).lat2
            }\n " +
                    "${
                        Geodesic.WGS84.Direct(
                            distanceMarks[1].latitude,
                            distanceMarks[1].longitude,
                            asin(projection / minLengthToColumn),
                            offset
                        ).lon2
                    } "
        )*/