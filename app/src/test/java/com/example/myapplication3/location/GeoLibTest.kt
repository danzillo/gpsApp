package com.example.myapplication3.location

import com.example.myapplication3.location.calc.*
import net.sf.geographiclib.*
import org.junit.Assert
import org.junit.Test
import kotlin.Pair

internal class GeoLibTest {

    // Заранее проверенные положения опорных точек
    private val knownDistanceMarks: Map<Int, Pair<Double, Double>> = mapOf(
        1 to Pair(804.11, 5.97),
        2 to Pair(1068.49, -3.30),
        3 to Pair(1132.85, 4.20)
    )

    // Тестовые точки на дороге
    data class TestPoint(
        val name: String,
        val coordinate: Coordinate,
        val kmPlusOffset: KmPlusOffset
    )

    private val points: List<TestPoint> = listOf(
        TestPoint(
            name = "Начало парковки (0+360, R 9.5)",
            coordinate = Coordinate(84.9333383060032, 56.4488557703840),
            kmPlusOffset = KmPlusOffset(0, 360.0, 9.5)
        ),
        TestPoint(
            name = "Не доезжая ЛЭП (0+785, R 5.7)",
            coordinate = Coordinate(84.939519790763, 56.4471799709039),
            kmPlusOffset = KmPlusOffset(0, 785.0, 5.7)
        ),
        TestPoint(
            name = "Чуть позже ЛЭП (1+021 R 4.3)",
            coordinate = Coordinate(84.9401600146328, 56.4471080939880),
            kmPlusOffset = KmPlusOffset(1, 021.3, 4.3)
        ),
        TestPoint(
            name = "Съезд в поле направо после ЛЭП (1+524 R 7.9)",
            coordinate = Coordinate(84.9480027570299, 56.4459193406610),
            kmPlusOffset = KmPlusOffset(1, 524.5, 7.9)
        ),
        TestPoint(
            name = "Съезд после ЛЭП в поле налево (1+812 L 8.3)",
            coordinate = Coordinate(84.9521063373097, 56.4447347087405),
            kmPlusOffset = KmPlusOffset(1, 812.4, -8.3)
        ),
        TestPoint(
            name = "Напротив дерева (1+1068; 2+000 R 4.4)",
            coordinate = Coordinate(84.9546837617013, 56.4429497990569),
            kmPlusOffset = KmPlusOffset(2, 0.0, 4.4)
        ),
        TestPoint(
            name = "Съезд налево после дерева 1 (2+330 L 13.1)",
            coordinate = Coordinate(84.9565129930468, 56.4401790136846),
            kmPlusOffset = KmPlusOffset(2, 330.7, -13.1)
        ),
        TestPoint(
            name = "Съезд после дерева налево 2 (2+963 L 12.2)",
            coordinate = Coordinate(84.9579139623627, 56.4345738705636),
            kmPlusOffset = KmPlusOffset(2, 962.5, -12.2)
        ),
        TestPoint(
            name = "Дача (3+448 R 5.5)",
            coordinate = Coordinate(84.9586023413717, 56.4291355996175),
            kmPlusOffset = KmPlusOffset(3, 448.6, 5.5)
        )
    )

    @Test
    fun testShiftAndOffsetCalc() {
        val r1 = shiftAndOffsetCalc(axis, points[0].coordinate)
        println("r1 = $r1")
        //Assert.assertEquals(points[0].kmPlusOffset.meter, r1.shift, 0.2)

        val r2 = shiftAndOffsetCalc(axis, points[1].coordinate)
        println("r2 = $r2")
        Assert.assertEquals(points[1].kmPlusOffset.meter, r2.shift, 0.2)

        val l1 = points[4].kmPlusOffset.meter + knownDistanceMarks[1]!!.first
        val r5 = shiftAndOffsetCalc(axis, points[4].coordinate)
        println("r5 = $r5, l1 = $l1")
        Assert.assertEquals(l1, r5.shift, 0.2)
    }

    @Test
    fun findOffsetSum() {
        val r1 = shiftAndOffsetCalc(axis, distanceMarks[0])
        println("Coord ${r1.crossPoint.latitude}  SHift ${r1.shift} Offset ${r1.offset}")
        /* println("r1 = $r1")
        println(r1.offset)*/
        val r2 = shiftAndOffsetCalc(axis, distanceMarks[1])
        println("Coord ${r2.crossPoint.latitude}  SHift ${r2.shift} Offset ${r2.offset}")
        /*   println("r1 = $r2")
        println(r2.offset)*/
        val r3 = shiftAndOffsetCalc(axis, myPosition[2])
        val r4 = shiftAndOffsetCalc(axis, myPosition[3])
        /* println("r1 = $r3")
        println(r3.offset)*/
    }

    @Test
    fun testAzimuthSign() {
        val g1 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            54.0, 85.1
        )
        println("g1 → ${g1.azi1}")
        Assert.assertEquals(90.0, g1.azi1, 0.1)

        val g2 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            54.0, 84.9
        )
        println("g2 ← ${g2.azi1}")
        Assert.assertEquals(-90.0, g2.azi1, 0.1)

        val g3 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            53.9, 84.9
        )
        println("g3 ↙ ${g3.azi1}")
        Assert.assertEquals(-149.4, g3.azi1, 0.1)

        val g4 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            53.9, 85.1
        )
        println("g4 ↘ ${g4.azi1}")
        Assert.assertEquals(149.4, g4.azi1, 0.1)
    }

    @Test
    fun roadKmSegment() {
        val r1 = roadKilometerSegment(axis, distanceMarks)

        val r2 = shiftAndOffsetCalc(axis, distanceMarks[0])
        Assert.assertEquals(
            r2.crossPoint.latitude,
            r1[0]?.segment?.lastIndex?.let { r1[0]?.segment?.get(it) }!!.latitude,
            0.1
        )

        val r3 = shiftAndOffsetCalc(axis, distanceMarks[1])
        Assert.assertEquals(
            r3.crossPoint.latitude,
            r1[1]?.segment?.lastIndex?.let { r1[1]?.segment?.get(it) }!!.latitude,
            0.1
        )

        val r4 = shiftAndOffsetCalc(axis, distanceMarks[2])
        Assert.assertEquals(
            r4.crossPoint.latitude,
            r1[1]?.segment?.lastIndex?.let { r1[1]?.segment?.get(it) }!!.latitude,
            0.1
        )

        Assert.assertEquals(
            axis[axis.lastIndex].latitude,
            r1[3]?.segment?.lastIndex?.let { r1[3]?.segment?.get(it) }!!.latitude,
            0.1
        )
    }

    @Test
    fun closeKmPoint() {
        // Добавляем к км столбам начальную и конечную точку оси дороги
        kmPlusMeter(roadKilometerSegment(axis, distanceMarks))

       val r1 = roadKilometerSegment(axis, distanceMarks)
        r1[0]?.kmPoints?.let { println(it[0].latitude) }
        r1[0]?.kmPoints?.let { println(it[1].latitude) }
        r1[0]?.kmPoints?.let { println(it[2].latitude) }
        r1[0]?.kmPoints?.let { println(it[3].latitude) }
    }
}


/*// Для поиска расстояния от начала координат до проекции столбов
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

            *//* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) *//*
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
            *//* Если найденное расстояние меньше того, что было, то сохраняем его с длиной
                участков дороги (от данной вершины до следующей и от данной вершины до предыдущей) *//*
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
}*/

