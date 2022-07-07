package com.example.myapplication3.location.calc

import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import kotlin.math.*

data class ShiftAndOffset(
    val shift: Double,
    val offset: Double,
    val crossPoint: Coordinate,
    val prevPoint: Int,
    val nextPoint: Int
) {
    override fun toString() =
        "<ShiftAndOffset> {shift: $shift, offset: $offset, lat: ${crossPoint.latitude}, long:  ${crossPoint.longitude}}"
}

fun shiftAndOffsetCalc(
    axis: MutableList<Coordinate>,
    point: Coordinate
): ShiftAndOffset {
    var minLengthToPoint =
        Double.MAX_VALUE  // Хранит минимальное расстояние между вершиной и столбом
    var numOfMinVertex = 0 // Номер вершины от которой расстояние минимально
    var numOfMinSeg = 0 // Номер сегмента от которого расстояние минимально
    val pointData = mutableListOf<GeodesicData>() // Хранит гео-инф. о вершинах и столбах
    val segmentData = mutableListOf<GeodesicData>()  // Хранит гео-инф. о вершинах
    val projection: Double // Проекция перпендикуляра столба
    val coordinateData: GeodesicData // Хранит инф. о координатах проекции столба
    var offset: Double = 0.0 // Инфо о смещении
    var currentLength = 0.0
    var totalLengthBtSegment = 0.0
    var nextPoint = -1
    var prevPoint = -1

    for (axisCounter in 0..axis.lastIndex) {

        // Считаем и сохраняем все данные (азимут, расстояние от вершины до заданной точкой
        val geodesicData = Geodesic.WGS84.Inverse(
            axis[axisCounter].latitude,
            axis[axisCounter].longitude,
            point.latitude,
            point.longitude
        )
        pointData.add(geodesicData)
        // Находим минимальное расстояние между вершиной и заданной точкой
        if (minLengthToPoint > geodesicData.s12) {
            minLengthToPoint = geodesicData.s12
            numOfMinVertex = axisCounter

            // Сохраняем длину от 0 до точки в близи столба
            totalLengthBtSegment = currentLength
        }

        if (axisCounter < axis.lastIndex) {
            // Считаем и сохраняем все данные от вершины до вершины
            segmentData.add(
                Geodesic.WGS84.Inverse(
                    axis[axisCounter].latitude,
                    axis[axisCounter].longitude,
                    axis[axisCounter + 1].latitude,
                    axis[axisCounter + 1].longitude
                )
            )
            // Общая длина складывается из текущей длины участка
            currentLength += segmentData[axisCounter].s12
        }

    }

    //TODO: Учесть «слепой угол»
    //TODO: Расчёт высоты в треугольнике с помощью sin

    // Определяем угол между следующим сегментом оси и вектором на исходную точку
    // для последующего определения способа расчёта смещения и его знака

    numOfMinSeg = numOfMinVertex
    if (numOfMinVertex == axis.lastIndex) numOfMinSeg -= 1

    // Определяем с какой стороны от крайней точки находится столб
    val listSymbol =
        checkOffsetAndColumnPlace(
            (segmentData[numOfMinSeg].azi1),
            pointData[numOfMinVertex].azi1
        )

    // Если столбец является крайним
    if (numOfMinVertex != numOfMinSeg) {

        // Если столбец стоит за осью дороги
        if (listSymbol[1]) {
            offset = minLengthToPoint
            if (!listSymbol[0]) {
                offset *= -1
            }

            coordinateData = Geodesic.WGS84.Inverse(
                point.latitude,
                point.longitude,
                point.latitude,
                point.longitude
            )

            prevPoint = numOfMinVertex
            nextPoint = numOfMinVertex
        }

        // Если столбец стоит перед столбцом
        else {
            offset = findOffset(
                pointData[numOfMinVertex - 1].s12,
                minLengthToPoint,
                segmentData[numOfMinSeg].s12
            )
            projection = findProjectionLength(
                minLengthToPoint, offset
            )

            coordinateData = Geodesic.WGS84.Direct(
                segmentData[numOfMinSeg].lat1,
                segmentData[numOfMinSeg].lon1,
                segmentData[numOfMinSeg].azi1,
                projection
            )

            if (!listSymbol[0]) {
                offset *= -1
            }

            prevPoint = numOfMinSeg
            nextPoint = numOfMinVertex
        }

    }
    else{
    // Если true - означает, что точка находится спереди от вершины
    if (listSymbol[1]) {

        // Проверяем что точка находится в пределах оси
        if (numOfMinVertex < axis.lastIndex) {

            // Рассчитываем ближайшее расстояние от точки до оси
            offset = findOffset(
                pointData[numOfMinVertex + 1].s12,
                minLengthToPoint,
                segmentData[numOfMinVertex].s12
            )

            // Рассчитываем расстояние от ближайшей вершины до пересечения
            projection = findProjectionLength(
                minLengthToPoint, offset
            )

            // Если проекция столба справа, то -1
            if (!listSymbol[0]) {
                offset *= -1
            }

            coordinateData = Geodesic.WGS84.Direct(
                segmentData[numOfMinVertex].lat1,
                segmentData[numOfMinVertex].lon1,
                segmentData[numOfMinVertex].azi1,
                projection
            )
            totalLengthBtSegment += projection
        } else {
            offset = minLengthToPoint
            coordinateData = GeodesicData()
        }
    } else {
        if (numOfMinVertex > 0) {
            // Пересечение перпендикуляра до сегмента
            offset = findOffset(
                pointData[numOfMinVertex - 1].s12,
                minLengthToPoint,
                segmentData[numOfMinVertex - 1].s12
            )

            projection = findProjectionLength(
                minLengthToPoint, offset
            )
            if (!listSymbol[0]) {
                offset *= -1
            }
            if (numOfMinVertex > 0) {
                prevPoint = numOfMinVertex - 1
                nextPoint = numOfMinVertex
            } else {
                prevPoint = numOfMinVertex
                if (numOfMinVertex < axis.lastIndex)
                    nextPoint = numOfMinVertex + 1
            }

            coordinateData = Geodesic.WGS84.Direct(
                segmentData[numOfMinVertex].lat1,
                segmentData[numOfMinVertex].lon1,
                segmentData[numOfMinVertex - 1].azi2 + 180,
                projection
            )
            totalLengthBtSegment -= projection

        } else {
            offset = minLengthToPoint
            coordinateData = GeodesicData()
        }
    }}
    return ShiftAndOffset(
        shift = totalLengthBtSegment,
        offset = offset,
        crossPoint = Coordinate(coordinateData.lon2, coordinateData.lat2),
        prevPoint = prevPoint,
        nextPoint = nextPoint
    )
}

/**
 * Функция для нахождения смещения от дороги
 * @param length - расстояние от (пред или след) вершины до столба
 * @param lengthMin - минимальное расстояние от вершины
 * @param lengthRoad - длина сегмента оси между двумя вершинами (одна из них - мин)
 * @return длина перпендикуляра
 */

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

// Поиск угла между азимутами двух векторов
private fun findAngle(
    firstAngle: Double,
    secondAngle: Double
): Double {
    return firstAngle - secondAngle
}

// Находит размерность смежного угла, и вычисляет промежуток в котором находится "слепой угол"
private fun adjacentAngle(
    firstAngle: Double,
    secondAngle: Double
): MutableList<Double> {
    val adjacentAngle = (360 - 2 * findAngle(firstAngle, secondAngle)) / 2

    return if ((firstAngle) > (secondAngle)) {
        mutableListOf(
            ((firstAngle) + adjacentAngle),
            ((secondAngle) - adjacentAngle)
        )
    } else {
        mutableListOf(
            ((firstAngle) - adjacentAngle),
            ((secondAngle) + adjacentAngle)
        )
    }
}

private fun convertMeterToKilometer(meters: Double): Int {
    return (meters / 1000).toInt()
}

private fun checkOffsetAndColumnPlace(segmentAz: Double, pointAz: Double): MutableList<Boolean> {
    // Определяет знак смещения -1 справа +1 слева
    var offsetSymbol = false
    // Определяет смещение относительно столба -1 = столб справа столб слева 1
    var columnPos = false
    val listSymbol = mutableListOf<Boolean>()
    if (segmentAz > 0) {

        // Все что внутри, то +, снаружи -!
        val firstBoard = segmentAz
        val secondBoard = segmentAz - 180
        val thirdBoard = segmentAz - 90

        offsetSymbol = pointAz < firstBoard && pointAz > secondBoard

        columnPos = if (pointAz < firstBoard && pointAz > thirdBoard) {
            true
        } else if (pointAz < thirdBoard && pointAz > secondBoard)
            false
        else !(pointAz < secondBoard && pointAz > (secondBoard - firstBoard) || pointAz <= 180 && pointAz > (180 - abs(
            thirdBoard
        )))

    } else {
        // Все что внутри это -, снаружи +!
        val firstBoard = segmentAz
        val secondBoard = segmentAz + 180
        val thirdBoard = segmentAz + 90

        offsetSymbol = !(pointAz > firstBoard && pointAz < secondBoard) //слева

        columnPos = if (pointAz > firstBoard && pointAz < thirdBoard) {
            true
        } else if (pointAz > thirdBoard && pointAz < secondBoard)
            false
        else pointAz >= secondBoard && pointAz <= secondBoard + abs(firstBoard) || pointAz >= -180 && pointAz > (-180 + abs(
            thirdBoard
        )
                )
    }

    listSymbol.add(offsetSymbol)
    listSymbol.add(columnPos)
    return listSymbol
}
