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
        var counter = 0.0
        for (vertexCounter in 0 until lastCoordinateIndex - 1) {

            // Считаем длину суммарную длину дороги между вершинами
            counter = Geodesic.WGS84.Inverse(
                axis[vertexCounter].latitude,
                axis[vertexCounter].longitude,
                axis[vertexCounter + 1].latitude,
                axis[vertexCounter + 1].longitude
            ).s12

            // Считаем общее расстояние дороги
        }
        assertEquals(3625.184, counter, 0.01)
    }

    @Test
    fun testGeoLibPoints() {
        // println(findNearPoint(axis, distanceMarks).get(0).length)
        // geoLibKilometersCalc(axis, distanceMarks)
        // println(columnMap[1])
        //  checkCloseMark(myPosition, columnMap)
        findNearPoint(axis, distanceMarks)
        println(lengthAndIndex.get(3).lastIndex)
    }

    // Поиск длины между столбами и точки
    private fun swiftAndOffset(axis: MutableList<Coordinate>, point: MutableList<Coordinate>) {
        // Для сохранения расстояний до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0
        var previousLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var offset = 0.0
        var totalLengthOfRoadToColumn = 0.0
        var totalRoadLength = 0.0
        var currentRoadLength: Double
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0
        var prevAzimuth = 0.0
        var azimuth = 0.0

        var projection: Double = 0.0

        for (markNum in 0 until point.lastIndex + 1) {
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

            // Проходимся по всем вершинам
            for (counter in 0 until axis.lastIndex) {

                // Находим расстояние от каждой вершины до столба
                lengthToColumn =
                    Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        point[markNum].latitude,
                        point[markNum].longitude
                    ).s12

                // Считаем длину дороги между двумя вершинами
                currentRoadLength = Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12

                // Считаем суммарную длину дороги между вершинами
                totalRoadLength += currentRoadLength

                /* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                    участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) */
                if (lengthToColumn < minLengthToColumn) {

                    // Сохранение текущего состояния
                    minLengthToColumn = lengthToColumn

                    // Длина до столба от следующей вершины
                    nextLengthToColumn = Geodesic.WGS84.Inverse(
                        axis[counter + 1].latitude,
                        axis[counter + 1].longitude,
                        point[markNum].latitude,
                        point[markNum].longitude
                    ).s12

                    // Длина текущего отрезка дороги
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
                                point[markNum].latitude,
                                point[markNum].longitude
                            ).s12

                        prevAzimuth = Geodesic.WGS84.Inverse(
                            axis[counter].latitude,
                            axis[counter].longitude,
                            axis[counter - 1].latitude,
                            axis[counter - 1].longitude
                        ).azi1
                    }
                    // Записываем длину дороги до вершины около столба
                    lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength

                    // Записываем координату ближайшей точки к столбу и ее азимут (для 2ух участков)
                    columnMap[markNum] = Column(
                        axis[counter].longitude,
                        axis[counter].latitude,
                        Geodesic.WGS84.Inverse(
                            axis[counter].latitude,
                            axis[counter].longitude,
                            axis[counter + 1].latitude,
                            axis[counter + 1].longitude
                        ).azi1,
                        prevAzimuth
                    )
                }
            }

            // Косинус угла между столбом и вершиной дороги
            val nextCosinus: Double =
                ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                    2
                )) // 2 * minLengthToColumn * nextSectionRoadLength
                        )

            if (nextCosinus >= 0.0 && nextSectionRoadLength != 0.0) {
                projection = findProjectionLength(
                    minLengthToColumn,
                    findOffset(
                        nextLengthToColumn,
                        minLengthToColumn,
                        nextSectionRoadLength,
                    )
                )
                totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex + projection
                offset = findOffset(
                    nextLengthToColumn,
                    minLengthToColumn,
                    nextSectionRoadLength,
                )
                azimuth = columnMap[markNum]?.azimuthNext!!
                columnMap[markNum]?.azimuthPrev = 0.0
            } else if (nextCosinus < 0.0 && previousSectionRoadLength != 0.0) {
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
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
                azimuth = columnMap[markNum]?.azimuthPrev!!
                columnMap[markNum]?.azimuthNext = 0.0
            } else println("Невозможно рассчитать КМ+М для данного столба.")
        }
    }

    // Расстояние до точки и ее смещение
    private fun findNearPoint(
        axis: MutableList<Coordinate>,
        point: MutableList<Coordinate>
    ): MutableList<LengthAndIndex> {
        var length: Double
        var minLength: Double
        var nearPoint = 0
        var totalLength: Double
        var lengthToPoint = 0.0
        var lengthBetweenPoint = 0.0
        for (pointCounter in 0 until point.lastIndex + 1) {
            minLength = Double.MAX_VALUE
            totalLength = 0.0
            for (axisCounter in 0 until axis.lastIndex) {
                length = Geodesic.WGS84.Inverse(
                    axis[axisCounter].latitude,
                    axis[axisCounter].longitude,
                    point[pointCounter].latitude,
                    point[pointCounter].longitude
                ).s12
                lengthBetweenPoint = Geodesic.WGS84.Inverse(
                    axis[axisCounter].latitude,
                    axis[axisCounter].longitude,
                    axis[axisCounter + 1].latitude,
                    axis[axisCounter + 1].longitude
                ).s12

                totalLength += lengthBetweenPoint
                //  println(  totalLength)
                if (minLength > length) {
                    minLength = length
                    nearPoint = axisCounter
                    lengthToPoint = totalLength
                    // println(lengthToPoint)
                }
            }

            lengthAndIndex.add(LengthAndIndex(nearPoint, lengthToPoint))
        }
        return lengthAndIndex
    }

    // Для поиска расстояния от начала координат до проекции столбов
    // получает координаты дороги и конкретный километровый столб
    private fun geoLibKilometersCalc(
        axis: MutableList<Coordinate>,
        point: MutableList<Coordinate>
    ) {
        // Для сохранения расстояний до столба
        var lengthToColumn: Double
        var minLengthToColumn: Double = Double.MAX_VALUE
        var nextLengthToColumn = 0.0
        var previousLengthToColumn = 0.0

        // Для сохранения длин отрезков между вершинами
        var offset = 0.0
        var totalLengthOfRoadToColumn = 0.0
        var totalRoadLength = 0.0
        var currentRoadLength: Double
        var lengthOfRoadToColumnVertex = 0.0
        var nextSectionRoadLength = 0.0
        var previousSectionRoadLength = 0.0
        var prevAzimuth = 0.0
        var azimuth = 0.0

        var projection: Double = 0.0

        for (markNum in 0 until point.lastIndex + 1) {
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

            // Проходимся по всем вершинам
            for (counter in 0 until axis.lastIndex) {

                // Находим расстояние от каждой вершины до столба
                lengthToColumn =
                    Geodesic.WGS84.Inverse(
                        axis[counter].latitude,
                        axis[counter].longitude,
                        point[markNum].latitude,
                        point[markNum].longitude
                    ).s12

                // Считаем длину дороги между двумя вершинами
                currentRoadLength = Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12

                // Считаем суммарную длину дороги между вершинами
                totalRoadLength += currentRoadLength

                /* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                    участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) */
                if (lengthToColumn < minLengthToColumn) {

                    // Сохранение текущего состояния
                    minLengthToColumn = lengthToColumn

                    axisDiv

                    // Длина до столба от следующей вершины
                    nextLengthToColumn = Geodesic.WGS84.Inverse(
                        axis[counter + 1].latitude,
                        axis[counter + 1].longitude,
                        point[markNum].latitude,
                        point[markNum].longitude
                    ).s12

                    // Длина текущего отрезка дороги
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
                                point[markNum].latitude,
                                point[markNum].longitude
                            ).s12

                        prevAzimuth = Geodesic.WGS84.Inverse(
                            axis[counter].latitude,
                            axis[counter].longitude,
                            axis[counter - 1].latitude,
                            axis[counter - 1].longitude
                        ).azi1
                    }

                    // Записываем длину дороги до вершины около столба
                    lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength

                    // Записываем координату ближайшей точки к столбу и ее азимут (для 2ух участков)
                    columnMap[markNum] = Column(
                        axis[counter].longitude,
                        axis[counter].latitude,
                        Geodesic.WGS84.Inverse(
                            axis[counter].latitude,
                            axis[counter].longitude,
                            axis[counter + 1].latitude,
                            axis[counter + 1].longitude
                        ).azi1,
                        prevAzimuth
                    )
                }
            }

            // Косинус угла между столбом и вершиной дороги
            val nextCosinus: Double =
                ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                    2
                )) / 2 * minLengthToColumn * nextSectionRoadLength)

            if (nextCosinus >= 0.0 && nextSectionRoadLength != 0.0) {
                projection = findProjectionLength(
                    minLengthToColumn,
                    findOffset(
                        nextLengthToColumn,
                        minLengthToColumn,
                        nextSectionRoadLength,
                    )
                )
                totalLengthOfRoadToColumn = lengthOfRoadToColumnVertex + projection
                offset = findOffset(
                    nextLengthToColumn,
                    minLengthToColumn,
                    nextSectionRoadLength,
                )
                azimuth = columnMap[markNum]?.azimuthNext!!
                columnMap[markNum]?.azimuthPrev = 0.0
            } else if (nextCosinus < 0.0 && previousSectionRoadLength != 0.0) {
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
                    previousLengthToColumn,
                    minLengthToColumn,
                    previousSectionRoadLength,
                )
                azimuth = columnMap[markNum]?.azimuthPrev!!
                columnMap[markNum]?.azimuthNext = 0.0
            } else println("Невозможно рассчитать КМ+М для данного столба.")

            println(
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
                        "\nOffset: $offset"
            )

            println(
                "Широта проекции: ${
                    columnMap[markNum]?.let {
                        Geodesic.WGS84.Direct(
                            it.latitude,
                            it.longitude,
                            azimuth,
                            projection
                        ).lat2
                    }
                }\n" +
                        "Долгота проеции: ${
                            columnMap[markNum]?.let {
                                Geodesic.WGS84.Direct(
                                    it.latitude,
                                    it.longitude,
                                    azimuth,
                                    projection
                                ).lon2
                            }
                        }\n"
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

    // Ищет 2 ближайших столбика к положению пользователя
    private fun checkCloseMark(
        myPos: MutableList<Coordinate>,
        column: MutableMap<Int, Column>
    ) {
        var firstColumn = 0
        var secondColumn = 0
        var currentLength = 0.0
        var prevLength = 0.0
        var nextLength = 0.0
        var currentColumn = 0

        var minLength = Double.MAX_VALUE

        for (columnCounter in 0 until column.size - 1) {
            currentLength = Geodesic.WGS84.Inverse(
                myPos[0].latitude,
                myPos[0].longitude,
                column[columnCounter]?.latitude!!,
                column[columnCounter]?.longitude!!
            ).s12
            println(currentLength)
            nextLength = Geodesic.WGS84.Inverse(
                myPos[0].latitude,
                myPos[0].longitude,
                column[columnCounter + 1]?.latitude!!,
                column[columnCounter + 1]?.longitude!!
            ).s12
            if (minLength > currentLength) {
                minLength = currentLength
                firstColumn = columnCounter
                secondColumn = if (columnCounter < column.size && prevLength > nextLength
                ) columnCounter - 1
                else columnCounter + 1
                currentColumn =
                    if (secondColumn > firstColumn && nextLength > currentLength) firstColumn else firstColumn - 1
            }
            prevLength = currentLength
        }
        println(currentColumn) // new column
    }
}