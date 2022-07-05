package com.example.myapplication3.location

import org.junit.Test
import net.sf.geographiclib.*
import kotlin.math.abs
import kotlin.math.pow

internal class GeoLibTest {

    @Test
    fun testGeoLibPoints() {
        shiftAndOffsetCalc(axis, myPosition[0])
       println(shiftAndOffsetCalc(axis, myPosition[0]).crossPoint.longitude)
    }

    private fun shiftAndOffsetCalc(
        axis: MutableList<Coordinate>,
        point: Coordinate
    ): ShiftAndOffset {
        var minLengthToColumn =
            Double.MAX_VALUE  // Хранит минимальное расстояние между вершиной и столбом
        var numOfMinVertex = 0 // Номер вершины от которой расстояние минимально
        val columnData = mutableListOf<GeodesicData>() // Хранит гео-инф. о вершинах и столбах
        val segmentData = mutableListOf<GeodesicData>()  // Хранит гео-инф. о вершинах
        val projection: Double // Проекция перпендикуляра столба
        val coordinateData: GeodesicData // Хранит инф. о координатах проекции столба
        var offset: Double // Инфо о смещении

        for (axisCounter in 0 until axis.lastIndex + 1) {

            // Считаем и сохраняем все данные от вершины до столба
            columnData.add(
                Geodesic.WGS84.Inverse(
                    axis[axisCounter].latitude,
                    axis[axisCounter].longitude,
                    point.latitude,
                    point.longitude
                )
            )

            if (axisCounter < axis.lastIndex)
            // Считаем и сохраняем все данные от вершины до вершины
                segmentData.add(
                    Geodesic.WGS84.Inverse(
                        axis[axisCounter].latitude,
                        axis[axisCounter].longitude,
                        axis[axisCounter + 1].latitude,
                        axis[axisCounter + 1].longitude
                    )
                )
        }

        // Находим минимальное расстояние между двумя вершинами
        columnData.forEachIndexed { index, element ->
            if (minLengthToColumn > element.s12) {
                minLengthToColumn = element.s12
                numOfMinVertex = index
            }
        }

        // Определяем косинус для последующего определения способа рассчета смещения и его знака
        val cosBtSegCol = segmentData[numOfMinVertex].azi1 - columnData[numOfMinVertex].azi1
        if (cosBtSegCol < 90 && cosBtSegCol >= 270) {
            offset = findOffset(
                columnData[numOfMinVertex + 1].s12,
                minLengthToColumn,
                segmentData[numOfMinVertex].s12
            )

            if (cosBtSegCol >= 270) offset = -offset
            projection = findProjectionLength(
                minLengthToColumn, offset
            )
            coordinateData = Geodesic.WGS84.Direct(
                segmentData[numOfMinVertex].lat1,
                segmentData[numOfMinVertex].lon1,
                segmentData[numOfMinVertex].azi1,
                projection
            )
        } else {
            offset = findOffset(
                columnData[numOfMinVertex - 1].s12,
                minLengthToColumn,
                segmentData[numOfMinVertex - 1].s12
            )
            if (cosBtSegCol >= 180) offset = -offset
            projection = findProjectionLength(
                minLengthToColumn, offset
            )
            coordinateData = Geodesic.WGS84.Direct(
                segmentData[numOfMinVertex].lat1,
                segmentData[numOfMinVertex].lon1,
                segmentData[numOfMinVertex - 1].azi2,
                projection
            )
        }
        return ShiftAndOffset(Coordinate(coordinateData.lon1, coordinateData.lat1), offset)
    }

}

class ShiftAndOffset(
    val crossPoint: Coordinate,
    val offset: Double
)

// Для поиска расстояния от начала координат до проекции столбов
// получает координаты дороги и конкретный километровый столб
private fun geoLibKilometersCalc(
    axis: MutableList<Coordinate>,
    distanceMarks: MutableList<Coordinate>
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
    var columnLat = 0.0
    var columnLong = 0.0

    var projection: Double = 0.0

    for (markNum in 0 until distanceMarks.lastIndex + 1) {
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
                    distanceMarks[markNum].latitude,
                    distanceMarks[markNum].longitude
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
                    distanceMarks[markNum].latitude,
                    distanceMarks[markNum].longitude
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
                            distanceMarks[markNum].latitude,
                            distanceMarks[markNum].longitude
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
                vertexCoordinate[markNum] = VertexCoordinate(
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

                // Записываем индекс ближайшего столба
                axisDiv[markNum] = AxisDiv(counter, lengthOfRoadToColumnVertex)
            }
        }

        // Косинус угла между столбом и вершиной дороги
        val nextCosinus: Double =
            ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                2
            )) / 2 * minLengthToColumn * nextSectionRoadLength)

        // В зависимости от значения косинуса рассчитываем проекцию столба и ее смещение
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
            azimuth = vertexCoordinate[markNum]?.azimuthNext!!
            vertexCoordinate[markNum]?.azimuthPrev = 0.0
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
            axisDiv[markNum] =
                axisDiv[markNum]?.let { AxisDiv(it.lastCoordIndex - 1, it.length) }!!
            // println(   axisDiv[markNum]?.lastCoord)
            azimuth = vertexCoordinate[markNum]?.azimuthPrev!!
            vertexCoordinate[markNum]?.azimuthNext = 0.0
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
            "КМ+М $totalLengthOfRoadToColumn"
        )

        columnLat = vertexCoordinate[markNum]?.let {
            Geodesic.WGS84.Direct(
                it.latitude,
                it.longitude,
                azimuth,
                projection,
            ).lat2
        }!!
        println("Широта" + columnLat)
        columnLong = vertexCoordinate[markNum]?.let {
            Geodesic.WGS84.Direct(
                it.latitude,
                it.longitude,
                azimuth,
                projection
            ).lon2
        }!!
        println("Dolgota" + columnLong + "\n")
        marksProjectionCoord[markNum] = MarksProjectionCoord(
            columnLat, columnLong, totalLengthOfRoadToColumn, offset
        )
        axisDiv[markNum]?.let { println(it.lastCoordIndex) }
    }
}

private fun findMeters(
    axisDiv: MutableMap<Int, AxisDiv>,
    point: MutableList<Coordinate>,
    nearPoint: Int
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
    var columnLat = 0.0
    var columnLong = 0.0

    var lat = 0.0
    var long = 0.0

    var projection: Double = 0.0

    val firstIndex =
        axisDiv[checkCloseMark(myPosition, marksProjectionCoord)]?.lastCoordIndex
    val secondIndex =
        axisDiv[checkCloseMark(myPosition, marksProjectionCoord) + 1]?.lastCoordIndex
    if (firstIndex != null && secondIndex != null) {


        axis.removeAt(firstIndex)
        axis.removeAt(secondIndex)

        // Долгота и широта проекции столба
        lat = marksProjectionCoord[nearPoint]?.latitude!!
        long = marksProjectionCoord[nearPoint]?.longitude!!
        axis.add(firstIndex, Coordinate(long, lat))

        // Долгота и широта проекции столба №2
        lat = marksProjectionCoord[nearPoint + 1]?.latitude!!
        long = marksProjectionCoord[nearPoint + 1]?.longitude!!
        axis.add(secondIndex, Coordinate(long, lat))

        for (counter in firstIndex until secondIndex) {

            // Находим расстояние от каждой вершины до столба
            lengthToColumn =
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    myPosition[0].latitude,
                    myPosition[0].longitude
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
            // println(lengthToColumn)
            /* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) */
            if (minLengthToColumn > lengthToColumn) {

                // Сохранение текущего состояния
                minLengthToColumn = lengthToColumn

                // Длина до столба от следующей вершины
                nextLengthToColumn = Geodesic.WGS84.Inverse(
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude,
                    myPosition[0].latitude,
                    myPosition[0].longitude
                ).s12

                // Длина текущего отрезка дороги
                nextSectionRoadLength = currentRoadLength

                if (counter > firstIndex) {
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
                            myPosition[0].latitude,
                            myPosition[0].longitude
                        ).s12

                }

                // Записываем длину дороги до вершины около столба
                lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength

            }

            val nextCosinus: Double =
                ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                    2
                )) / 2 * minLengthToColumn * nextSectionRoadLength)

            // В зависимости от значения косинуса рассчитываем проекцию столба и ее смещение
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
                // println(   axisDiv[markNum]?.lastCoord)
            }

        }
        println(totalRoadLength)
        println(marksProjectionCoord[nearPoint]?.length!!)
        println("Расстояние до человека: ${totalLengthOfRoadToColumn + (marksProjectionCoord[nearPoint]?.length!!)}")
        println(
            "КМ+М: ${nearPoint} +" +
                    " ${
                        abs(
                            (convertMeterToKilometer(totalLengthOfRoadToColumn)) * 1000 -
                                    (totalLengthOfRoadToColumn.toInt())
                        )
                    } " +
                    "\nOffset: $offset"
        )
    }
}

// Ищет 2 ближайших столбика к положению пользователя
private fun checkCloseMark(
    myPos: MutableList<Coordinate>,
    column: MutableMap<Int, MarksProjectionCoord>
): Int {
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
        // println(currentLength)
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

    //  println(currentColumn) // new column
    return currentColumn
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
