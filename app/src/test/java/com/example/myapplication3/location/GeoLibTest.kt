package com.example.myapplication3.location

import org.junit.Test
import net.sf.geographiclib.*
import org.junit.Assert.assertEquals
import kotlin.math.abs
import kotlin.math.asin
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
        geoLibKilometersCalc(axis, distanceMarks, 0)
//        geoLibKilometersCalc(axis, distanceMarks, 1)
//        geoLibKilometersCalc(axis, distanceMarks, 2)
//        geoLibKilometersCalc(axis, distanceMarks, 3)
//        geoLibKilometersCalc(axis, distanceMarks, 4)
        //println("Ближайший столбик: ${findNearestMark(myPosition, distanceMarks, 0)}")
        //val myLatPosList = decDegToDegMinSec(myPosition[0].latitude)
//        println("Ближайший столбик: ${findNearestMark(myPosition, 0, distanceMarks)}")
//        println(
//            "Ближайшая вершина: ${
//                findNearestMark(
//                    distanceMarks,
//                    findNearestMark(myPosition, 0, distanceMarks),
//                    axis
//                )
//            }"
//        )
    }

    private fun findNearestMark(
        currentPosition: MutableList<Coordinate>,
        currentPositionIndex: Int,
        positionList: MutableList<Coordinate>
    ): Int {

        // Координаты пользователя в градусах, минутах, секундах
        val myLatPosList = decDegToDegMinSec(currentPosition[0].latitude)
        val myLatDeg: Int = myLatPosList[0]
        val myLatMin: Int = myLatPosList[1]
        // val myLatSec: Int = myLatPosList[2]

        val myLongPosList = decDegToDegMinSec(currentPosition[0].longitude)
        val myLongDeg: Int = myLongPosList[0]
        val myLongMin: Int = myLongPosList[1]
        //  val myLongSec: Int = myLongPosList[2]

        // Для запоминания столба
        var saveCounter = 0

        for (counter in 0 until positionList.size - 1) {
            // Перевод координат столбов в минуты, секунды..
            val markLatPosList = decDegToDegMinSec(positionList[counter].latitude)
            val markLatDeg: Int = markLatPosList[0]
            val markLatMin: Int = markLatPosList[1]
            //val markLatSec: Int = markLatPosList[2]

            val markLongPosList = decDegToDegMinSec(positionList[counter].longitude)
            val markLongDeg: Int = markLongPosList[0]
            val markLongMin: Int = markLongPosList[1]
            //val markLongSec: Int = markLongPosList[2]

            // Если координаты пользователя и столба сходятся по градусам, то продолжаем поиск
            if (myLatDeg == markLatDeg && myLongDeg == markLongDeg && myLatMin == markLatMin && myLongMin == markLongMin) {
                saveCounter = counter
            } else continue
        }
        // Возвращает значение столба - 1, чтобы избежать ситуации, когда столб спереди
        return if (saveCounter > 1) saveCounter - 1
        else saveCounter
    }


    private fun decDegToDegMinSec(
        decDeg: Double
    ): List<Int> {
        val deg = decDeg.toInt()
        val min = ((decDeg - deg) * 60).toInt()
        val sec = ((decDeg - deg - (min.toDouble() / 60)) * 3600).toInt()
        return listOf<Int>(deg, min, sec)
    }


    // Для поиска расстояния от начала координат до проекции столбов
    // получает координаты дороги и конкретный километровый столб
    fun geoLibKilometersCalc(
        axis: MutableList<Coordinate>,
        distanceMarks: MutableList<Coordinate>,
        markNum: Int,
        //  myPosition: MutableList<Coordinate>
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

        var projection: Double = 0.0

        val counter = markNum

        val lastCoordinateIndex = axis.lastIndex

        // Проходимся по всем вершинам
        for (counter in 0 until lastCoordinateIndex - 1) {

            // Находим расстояние от каждой вершины до столба
            lengthToColumn =
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    distanceMarks[markNum].latitude,
                    distanceMarks[markNum].longitude
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
               // println(counter)
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
                }
                // Записываем длину дороги до вершины около столба
                lengthOfRoadToColumnVertex = totalRoadLength - nextSectionRoadLength
            }
        }

        // Косинус угла между столбом и вершиной дороги
        val nextCosinus: Double =
            ((minLengthToColumn.pow(2) + nextSectionRoadLength.pow(2) - nextLengthToColumn.pow(
                2
            )) / 2 * minLengthToColumn * nextSectionRoadLength)
        val nprefCosinus: Double =
            ((minLengthToColumn.pow(2) + previousSectionRoadLength.pow(2) - previousLengthToColumn.pow(
                2
            )) / 2 * minLengthToColumn * previousSectionRoadLength)

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
        } else println("Невозможно рассчитать КМ+М для данного столба.")

        println(
            "Высота: ${findOffset(
                previousLengthToColumn,
                minLengthToColumn,
                previousSectionRoadLength,
            )}"
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
            " ${Geodesic.WGS84.Direct(distanceMarks[1].latitude, distanceMarks[1].longitude,asin(projection/minLengthToColumn), offset).lat2}\n "+
            "${Geodesic.WGS84.Direct(distanceMarks[1].latitude, distanceMarks[1].longitude,asin(projection/minLengthToColumn), offset).lon2} "
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


//    /* Находит ближайший по координатам столб */
//    private fun findNearestMark(
//        myPosition: MutableList<Coordinate>,
//        distanceMarks: MutableList<Coordinate>,
//        x: Int
//    ): Int {
//        var compareDif: Double = Double.MAX_VALUE
//        var markCount = 0
//        //val accuracy = 0.01
//
//        for (counter in 0 until distanceMarks.size - 1) {
//
//            // Сравниваем разницу по долготе между столбом и позицией
//            // Алгоритм работает до тех пор, пока разница не начинает увелчиваться
//            if (compareDif > abs(myPosition[x].latitude - distanceMarks[counter].latitude)) {
//                compareDif = myPosition[x].latitude - distanceMarks[counter].latitude
//                markCount += 1
//            } else break
//            compareDif = abs(myPosition[x].latitude - distanceMarks[counter].latitude)
//        }
//        if (markCount > 0)
//            return markCount - 1
//        else return markCount
//    }