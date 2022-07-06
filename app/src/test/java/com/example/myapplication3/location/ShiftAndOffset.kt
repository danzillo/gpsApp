package com.example.myapplication3.location

import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import kotlin.math.*

class ShiftAndOffset(
    val crossPoint: Coordinate,
    val offset: Double,
    val shift: Double,
    val minVertex: Int
)

fun shiftAndOffsetCalc(
    axis: MutableList<Coordinate>,
    point: Coordinate
): ShiftAndOffset {
    var minLengthToPoint =
        Double.MAX_VALUE  // Хранит минимальное расстояние между вершиной и столбом
    var numOfMinVertex = 0 // Номер вершины от которой расстояние минимально
    val pointData = mutableListOf<GeodesicData>() // Хранит гео-инф. о вершинах и столбах
    val segmentData = mutableListOf<GeodesicData>()  // Хранит гео-инф. о вершинах
    val projection: Double // Проекция перпендикуляра столба
    val coordinateData: GeodesicData // Хранит инф. о координатах проекции столба
    var offset: Double // Инфо о смещении
    var currentLength = 0.0
    var totalLengthBtSegment = 0.0
    for (axisCounter in 0..axis.lastIndex) {

        // Считаем и сохраняем все данные (азимут, расстояние от вершины до столба
        pointData.add(
            Geodesic.WGS84.Inverse(
                axis[axisCounter].latitude,
                axis[axisCounter].longitude,
                point.latitude,
                point.longitude
            )
        )

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
            // Находим минимальное расстояние между двумя вершинами
            if (minLengthToPoint > pointData[axisCounter].s12) {
                minLengthToPoint = pointData[axisCounter].s12
                numOfMinVertex = axisCounter

                // Считаем длину от 0 до точки в близи столба
                totalLengthBtSegment = currentLength
            }
            // Общая длина складывается из текущей длины участка
            currentLength += segmentData[axisCounter].s12
        }
    }

    //TODO: Учесть «слепой угол»

    val cosinus: Double =
        (minLengthToPoint.pow(2) + segmentData[numOfMinVertex].s12.pow(2) - pointData[numOfMinVertex + 1].s12.pow(
            2
        )) / (2 * minLengthToPoint * segmentData[numOfMinVertex].s12)

    // Определяем угол между следующим сегментом оси и вектором на исходную точку
    // для последующего определения способа расчёта смещения и его знака
    val angleBtSegPoint =
        abs(
            translateToFullCircle(segmentData[numOfMinVertex].azi1) - translateToFullCircle(
                pointData[numOfMinVertex].azi1
            )
        )
    println(translateToFullCircle(segmentData[numOfMinVertex].azi2))
    println(translateToFullCircle(pointData[numOfMinVertex].azi2))
    println(angleBtSegPoint)

    println(findAngle(segmentData[numOfMinVertex].azi2, pointData[numOfMinVertex].azi1))

    println(checkOffsetSymbol(segmentData[numOfMinVertex].azi2, pointData[numOfMinVertex].azi1))
    println(checkSide(segmentData[numOfMinVertex].azi2, pointData[numOfMinVertex].azi1))
    if (angleBtSegPoint < 90 && angleBtSegPoint >= 270) {
        // Пересечение перпендикуляра на сегменте (наверное)

        // Рассчитываем ближайшее расстояние от точки до оси
        offset = findOffset(
            pointData[numOfMinVertex + 1].s12,
            minLengthToPoint,
            segmentData[numOfMinVertex].s12
        )

        if (angleBtSegPoint >= 270) offset = -offset

        // Рассчитываем расстояние от ближайшей вершины до пересечения
        projection = findProjectionLength(
            minLengthToPoint, offset
        )
        coordinateData = Geodesic.WGS84.Direct(
            segmentData[numOfMinVertex].lat1,
            segmentData[numOfMinVertex].lon1,
            segmentData[numOfMinVertex].azi1,
            projection
        )
        totalLengthBtSegment += projection
    } else {
        // Пересечение перпендикуляра до сегмента
        offset = findOffset(
            pointData[numOfMinVertex - 1].s12,
            minLengthToPoint,
            segmentData[numOfMinVertex - 1].s12
        )
        if (angleBtSegPoint >= 180) offset = -offset
        projection = findProjectionLength(
            minLengthToPoint, offset
        )
        coordinateData = Geodesic.WGS84.Direct(
            segmentData[numOfMinVertex].lat1,
            segmentData[numOfMinVertex].lon1,
            segmentData[numOfMinVertex - 1].azi2 + 180,
            projection
        )
        totalLengthBtSegment -= projection
        numOfMinVertex -= 1
    }
    return ShiftAndOffset(
        Coordinate(coordinateData.lon2, coordinateData.lat2),
        offset,
        totalLengthBtSegment,
        numOfMinVertex
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

// Преобразует значения градусов в значения от 0 до 360
private fun translateToFullCircle(
    deg: Double
): Double {
    return if (deg > 360)
        deg - 360
    else if (deg < 0)
        deg + 360
    else deg
}

// Поиск угла между азимутами двух векторов
private fun findAngle(
    firstAngle: Double,
    secondAngle: Double
): Double {
    return translateToFullCircle(firstAngle) - translateToFullCircle(secondAngle)
}

// Находит размерность смежного угла, и вычисляет промежуток в котором находится "слепой угол"
private fun adjacentAngle(
    firstAngle: Double,
    secondAngle: Double
): MutableList<Double> {
    val adjacentAngle = (360 - 2 * findAngle(firstAngle, secondAngle)) / 2

    return if (translateToFullCircle(firstAngle) > translateToFullCircle(secondAngle)) {
        mutableListOf(
            translateToFullCircle(translateToFullCircle(firstAngle) + adjacentAngle),
            translateToFullCircle(translateToFullCircle(secondAngle) - adjacentAngle)
        )
    } else {
        mutableListOf(
            translateToFullCircle(translateToFullCircle(firstAngle) - adjacentAngle),
            translateToFullCircle(translateToFullCircle(secondAngle) + adjacentAngle)
        )
    }
}

// Проверяет в какую сторону от оси дороги смещение(true - влево false - вправо)
private fun checkOffsetSymbol(
    segmentAz: Double,
    pointAz: Double
): Boolean {
    // println("ras ${translateToFullCircle(segmentAz + 180)} dva" + translateToFullCircle(pointAz))
    if (translateToFullCircle(segmentAz + 180) > translateToFullCircle(segmentAz)) {
        return !(translateToFullCircle(segmentAz + 180) >= translateToFullCircle(pointAz) && translateToFullCircle(
            segmentAz
        ) <= translateToFullCircle(pointAz))
    } else (translateToFullCircle(segmentAz + 180) < translateToFullCircle(segmentAz))
    return !(translateToFullCircle(segmentAz + 180) <= translateToFullCircle(pointAz) && translateToFullCircle(
        segmentAz
    ) >= translateToFullCircle(pointAz))
}

private fun checkSide(
    segmentAz: Double,
    pointAz: Double
): Boolean {
    if ((translateToFullCircle(segmentAz + 180) > translateToFullCircle(segmentAz))) {
        if (translateToFullCircle(segmentAz + 90) > translateToFullCircle(segmentAz)) {
            if (translateToFullCircle(segmentAz + 90) >= translateToFullCircle(pointAz) && translateToFullCircle(
                    segmentAz
                ) < translateToFullCircle(pointAz)
            ) return true
            else return false
        }
        if (translateToFullCircle(segmentAz + 90) < translateToFullCircle(segmentAz)) {
            if (translateToFullCircle(segmentAz + 90) <= translateToFullCircle(pointAz) && translateToFullCircle(
                    segmentAz
                ) > translateToFullCircle(pointAz)
            ) return true
            else return false
        }
    } else (translateToFullCircle(segmentAz + 180) < translateToFullCircle(segmentAz))
    return !(translateToFullCircle(segmentAz + 180) <= translateToFullCircle(pointAz) && translateToFullCircle(
        segmentAz
    ) >= translateToFullCircle(pointAz))
}


private fun convertMeterToKilometer(meters: Double): Int {
    return (meters / 1000).toInt()
}
