package com.example.myapplication3.location.calc

import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import kotlin.math.abs
import kotlin.math.pow

/**
 * Класс для
 * - получения координат проекции
 * - расчёт длины до проекции
 * - расчёт
 */
class ShiftAndOffsetCalc {
    //TODO: (ПОТОМ) Накапливать сведения об и не считать каждый раз

    fun shiftAndOffsetCalc(
        axis: MutableList<Coordinate>,
        point: Coordinate,
    ): ShiftAndOffset {
        var minLengthToPoint =
            Double.MAX_VALUE  // Хранит минимальное расстояние между вершиной и столбом
        var numOfMinVertex = 0 // Номер вершины от которой расстояние минимально
        val pointData = mutableListOf<GeodesicData>() // Хранит гео-инф. о вершинах и столбах
        val segmentData = mutableListOf<GeodesicData>()  // Хранит гео-инф. о вершинах
        val projection: Double // Проекция перпендикуляра столба
        val coordinateData: GeodesicData // Хранит инф. о координатах проекции столба
        var currentLength = 0.0
        var totalLengthBtSegment = 0.0
        var blindOrNot = false
        var prevPoint: Int = -1
        var nextPoint: Int = -1
        var offset: Double = 0.0

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

        val minPoint = numOfMinVertex

        //TODO: Расчёт высоты в треугольнике с помощью sin

        // Определяем угол между следующим сегментом оси и вектором на исходную точку
        // для последующего определения способа расчёта смещения и его знака

        var numOfMinSeg: Int = numOfMinVertex // Номер сегмента от которого расстояние минимально
        if (numOfMinVertex == axis.lastIndex) numOfMinSeg -= 1

        // Определяем с какой стороны от крайней точки находится столб
        val listSymbol =
            checkOffsetAndColumnPlace(
                (segmentData[numOfMinSeg].azi1),
                pointData[numOfMinVertex].azi1
            )

        // Проверяем находится ли точка в слепой зоне
        if (numOfMinVertex > 0 && numOfMinVertex < axis.lastIndex) {
            val blindBoarder = blindAngleBoarder(
                segmentData[numOfMinVertex].azi1,
                segmentData[numOfMinVertex - 1].azi1
            )
            blindOrNot = isBlindAngle(blindBoarder, pointData[numOfMinVertex].azi1)

            print(blindOrNot)
        }

        // Является ли вершина крайней
        if (numOfMinVertex != numOfMinSeg) {

            // Если точка стоит за осью дороги
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
                totalLengthBtSegment = offset
            }

            // Если точка стоит перед крайней вершиной
            else {
                offset = findOffset(
                    pointData[numOfMinVertex - 1].s12,
                    minLengthToPoint,
                    segmentData[numOfMinVertex - 1].s12
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
                totalLengthBtSegment -= projection
            }

        } else {
            // Если точка находится в слепом угле, то
            if (blindOrNot) {

                // Находим смещение до точки
                offset = pointData[numOfMinVertex].s12
                // Если проекция столба справа, то -1
                if (!listSymbol[0]) {
                    offset *= -1
                }
                prevPoint = numOfMinVertex
                nextPoint = numOfMinVertex + 1
                // Определяем координаты пересечения с точкой
                pointData[numOfMinVertex].lon2 = pointData[numOfMinVertex].lon1
                pointData[numOfMinVertex].lat2 = pointData[numOfMinVertex].lat1
                coordinateData = pointData[numOfMinVertex]
            }
            // Если true - означает, что точка находится спереди от вершины
            else if (listSymbol[1]) {

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
                prevPoint = numOfMinVertex
                nextPoint = numOfMinVertex + 1
                totalLengthBtSegment += projection
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
            }
        }
        return ShiftAndOffset(
            shift = totalLengthBtSegment,
            offset = offset,
            crossPoint = Coordinate(coordinateData.lon2, coordinateData.lat2),
            prevPoint = prevPoint,
            nextPoint = nextPoint,
            minPoint = minPoint,
            totalLength = currentLength,
            isAheadPoint = listSymbol[1]
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

    /**
     * Функция для нахождения длины проекции
     * @param lengthMin - минимальное расстояние от вершины до точки(гипотенуза)
     * @param height - длина перпендикуляра(катет)
     * @return длина проекции(катет)
     */
    private fun findProjectionLength(
        lengthMin: Double,
        height: Double,
    ): Double {
        return (lengthMin.pow(2) - height.pow(2)).pow(0.5)
    }

    // Функция, которая позволяет менять значения угла к принятым в решении ед. от 180 до -180.
    private fun translateAngle(angle: Double): Double {
        return if (angle > 180) angle - 360
        else if (angle < -180) angle + 360
        else angle
    }

    // Находит границы слепого угла
    private fun blindAngleBoarder(
        firstAngle: Double,
        secondAngle: Double
    ): MutableList<Double> {
        val firstBoarder: Double
        val secondBoarder: Double

        // Если угол острый
        if (abs(translateAngle(firstAngle - secondAngle)) < 90) {
            firstBoarder = if (firstAngle >= 0) firstAngle - 180
            else firstAngle + 180
            secondBoarder = if (secondAngle >= 0) secondAngle - 180
            else secondAngle + 180
            return mutableListOf(firstBoarder, secondBoarder)
        } else {// Если угол тупой
            if (secondAngle > translateAngle(firstAngle + 180)) {
                firstBoarder = translateAngle(firstAngle + 90)
                secondBoarder = translateAngle(secondAngle - 90)
            } else {
                firstBoarder = translateAngle(firstAngle - 90)
                secondBoarder = translateAngle(secondAngle + 90)
            }
            return mutableListOf(firstBoarder, secondBoarder)
        }
    }

    // Находится ли точка в "слепом" угле
    private fun isBlindAngle(
        angles: MutableList<Double>,
        posAngle: Double
    ): Boolean {
        return if (angles[0] > angles[1] && angles[1] > 0 || angles[0] > angles[1] && angles[0] < 0) {
            posAngle > angles[1] && posAngle < angles[0]
        } else {
            posAngle < angles[1] && posAngle > angles[0]
        }
    }

    private fun checkOffsetAndColumnPlace(
        segmentAz: Double,
        pointAz: Double
    ): MutableList<Boolean> {
        // Определяет знак смещения -1 справа +1 слева
        val offsetSymbol: Boolean
        // Определяет смещение относительно столба -1 = столб справа столб слева 1
        val columnPos: Boolean
        val listSymbol = mutableListOf<Boolean>()
        if (segmentAz > 0) {
            // Все что внутри, то +, снаружи -!
            val firstBoarder = segmentAz
            val secondBoarder = segmentAz - 180
            val thirdBoarder = segmentAz - 90

            offsetSymbol = !(pointAz < firstBoarder && pointAz > secondBoarder)
            columnPos =
                if (pointAz in thirdBoarder..firstBoarder) {
                    true
                } else if (pointAz < thirdBoarder && pointAz >= secondBoarder)
                    false
                else !(pointAz < secondBoarder && pointAz >= (secondBoarder - firstBoarder) || pointAz <= 180 && pointAz > (180 - abs(
                    thirdBoarder
                )))
        } else {
            // Все что внутри это -, снаружи +!
            val firstBoarder = segmentAz
            val secondBoarder = segmentAz + 180
            val thirdBoarder = segmentAz + 90

            offsetSymbol = pointAz > firstBoarder && pointAz < secondBoarder //слева

            columnPos = if (pointAz > firstBoarder && pointAz < thirdBoarder) {
                true
            } else if (pointAz > thirdBoarder && pointAz < secondBoarder)
                false
            else pointAz >= secondBoarder && pointAz <= secondBoarder + abs(firstBoarder) || pointAz >= -180 && pointAz > (-180 + abs(
                thirdBoarder
            ))
        }

        listSymbol.add(offsetSymbol)
        listSymbol.add(columnPos)
        return listSymbol
    }

    private fun checkOffsetSymbol(
        segmentAz: Double,
        pointAz: Double
    ): Boolean {
        val offsetSymbol: Boolean
        offsetSymbol = !(pointAz < segmentAz && pointAz > translateAngle(segmentAz - 180))
        return offsetSymbol
    }

/*    private fun checkColumnPlace(
        segmentAz: Double,
        pointAz: Double
    ): Boolean {
        val columnPos: Boolean
        if (segmentAz > 0) {
            columnPos =
                if (pointAz in thirdBoarder..firstBoarder) {
                    true
                } else if (pointAz < thirdBoarder && pointAz >= secondBoarder)
                    false
                else !(pointAz < secondBoarder && pointAz >= (secondBoarder - firstBoarder) || pointAz <= 180 && pointAz > (180 - abs(
                    thirdBoarder
                )))
        }

        else
        return columnPos
    }*/
}
