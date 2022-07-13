package com.example.myapplication3.location.kmpluscalc.kmpluscalculator

import com.example.myapplication3.location.kmpluscalc.data.Coordinate
import com.example.myapplication3.location.kmpluscalc.data.Envelope
import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import kotlin.math.PI
import kotlin.math.cos

/**
 * Класс для хранения ломаной, расчёта координат по относительным смещениям и наоборот.
 * @param allPoints - последовательные координаты узлов ломаной
 * @param segmentDistances - массив расстояний в метрах до следующей точки
 * @param segmentAzimuths - массив азимутов до следующей точки
 * @param accumulatedDistances - массив накопленного расстояния в метрах от начала до каждой точек ломаной (кроме последней)
 * @param envelope - охват координат ломаной
 * @param length - общая длина ломаной в метрах
 */
class RoadSegment private constructor(
    private val allPoints: Array<Coordinate>,
    private val segmentDistances: Array<Double>,
    private val segmentAzimuths: Array<Double>,
    private val accumulatedDistances: Array<Double>,
    val envelope: Envelope,
    val length: Double
) {

    /**
     * Поиск пересечения перпендикуляра от точки к ломаной.
     * Базовый алгоритм для вычисления км+ из точки;
     * а также для разбиения оси дороги на километровые сегменты.
     * @param point - координата, для которой требуется найти перпендикуляр к ломаной
     * @return - структура с ответом: расстояние от оси, смещение вдоль оси, координата центра перпендикуляра
     */
    fun calcShiftAndOffsetFromLocation(point: Coordinate): ShiftAndOffset {
        // Поиск через оптимизирующую функцию
        return searchPerpendicularInRange(point)
    }

    /**
     * Поиск пересечения перпендикуляра от точки к участку ломаной.
     * Элемент оптимизационной стратегии.
     * @param point - координата, для которой требуется найти перпендикуляр к ломаной
     * @param beginIndex - индекс начала подмножества точек для поиска
     * @param endIndex - индекс конца подмножества точек для поиска
     * @return - структура с ответом: расстояние от оси, смещение вдоль оси, координата центра перпендикуляра
     */
    private fun searchPerpendicularInRange(point: Coordinate, beginIndex: Int = 0, endIndex: Int = allPoints.size-1): ShiftAndOffset {
        // Объект, где будем собирать результат поиска и улучшать его
        val result = ShiftAndOffset()

        // Оптимизация:
        // Если число точек > 1000 (если точки через 5-10м -> 5-10км участок)
        // Делим всю ломаную на три пересекающихся участка,
        // по центру каждого запрашиваем расстояние до заданной точки,
        // рекурсивно повторяем.
        // Если число точек < 1000 - проходим по всем
        //
        // Причина:
        // На М-4 имеем порядка 78000 точек, поиск одного перпендикуляра сплошным поиском занимает порядка 200 мс.
        // Даже разбиение на км сегменты будет обходиться более чем в 4 минуты.
        //

        val pointCount = endIndex - beginIndex + 1
        if (pointCount > 1000) {

            class SubRange(
                val centerIndex: Int,
                val beginIndex: Int,
                val endIndex: Int,
                var distanceToPoint: Double = Double.MAX_VALUE
            )

            // Определяем подмножества для поиска ближайшего
            val subRanges = listOf(
                SubRange(
                    centerIndex = beginIndex + pointCount / 4,
                    beginIndex = beginIndex,
                    endIndex = beginIndex + pointCount / 2
                ),
                SubRange(
                    centerIndex = beginIndex + pointCount / 2,
                    beginIndex = beginIndex + pointCount / 4,
                    endIndex = beginIndex + pointCount * 3 / 4
                ),
                SubRange(
                    centerIndex = beginIndex + pointCount * 3 / 4,
                    beginIndex = beginIndex + pointCount / 2,
                    endIndex = endIndex
                )
            )

            // Для каждого подмножества оцениваем расстояние
            subRanges.forEach { subRange ->
                // Рассчитаем геодезическое расстояние на поверхности между каждой точкой искомой
                val a = allPoints[subRange.centerIndex]
                val gd: GeodesicData = Geodesic.WGS84.Inverse(
                    a.y, a.x,
                    point.y, point.x
                )
                subRange.distanceToPoint = gd.s12
            }

            // Выбираем минимальную дистанцию подмножества к точке
            val nearestSubRange = subRanges.minByOrNull { it.distanceToPoint }!!

            // Запускаем поиск рекурсивно на заданном подмножестве
            return searchPerpendicularInRange(
                point,
                beginIndex = nearestSubRange.beginIndex,
                endIndex = nearestSubRange.endIndex
            )

        } else {

            // По циклу пройти все вершины, рассчитывая дистанцию между узлами и искомой точками
            val pointDistances = DoubleArray(pointCount)    // Запоминать расстояния
            val pointAzimuths = DoubleArray(pointCount)     // Запоминать азимуты
            var minDistance = Double.MAX_VALUE
            var minIndex = -1

            for (i in 0 until pointCount) {
                // точка исследуемого сегмента
                val a = allPoints[beginIndex + i]

                // Рассчитаем геодезическое расстояние на поверхности между каждой точкой искомой
                val gd: GeodesicData = Geodesic.WGS84.Inverse(
                    a.y, a.x,
                    point.y, point.x
                )
                val pointDistance = gd.s12
                val pointAzimuth = gd.azi1
                pointDistances[i] = pointDistance
                pointAzimuths[i] = pointAzimuth

                // Запомнить минимум и его индекс
                if (minDistance > pointDistance) {
                    minDistance = pointDistance
                    minIndex = beginIndex + i
                }
            }

            // В соседних к ближайшей точке сегментах поискать перпендикуляр
            var startIndex = minIndex-1
            var stopIndex = minIndex+1
            // Проверка границ
            if (startIndex < 0) startIndex = 0
            if (stopIndex > allPoints.size-1) stopIndex = allPoints.size-1

            for (i in startIndex until stopIndex) {
                // Начальная и конечная точки исследуемого сегмента
                val a = allPoints[i]
                val b = allPoints[i+1]

                // Берём геодезическое расстояние на поверхности между начальной точкой и заданной
                val distancePointToA = pointDistances[i - beginIndex]
                var azimuthPointToA = pointAzimuths[i - beginIndex]

                // Берём геодезическое расстояние на поверхности между точками сегмента
                val sectionLen = segmentDistances[i]
                var sectionAzimuth = segmentAzimuths[i]

                // Нормализуем углы в диапазон 0…360°
                if (azimuthPointToA<0) azimuthPointToA += 360.0
                if (sectionAzimuth<0) sectionAzimuth += 360.0

                // Оценить расположение перпендикуляра на сегменте —
                // находим разницу азимутов.
                var deltaAzimuth = azimuthPointToA - sectionAzimuth
                if (deltaAzimuth<0) deltaAzimuth += 360.0

                // Получаем расстояние до основания перпендикуляра
                val subLen = cos(PI * deltaAzimuth / 180) * distancePointToA

                // Получаем ранее рассчитанное расстояние вдоль оси до точки a
                val accumulatedLength = accumulatedDistances[i]

                // Функция проверки точки
                fun checkForMin(pointOnPolyline: Coordinate, meter: Double, segmentNum: Int, offset: Double) {
                    // Запоминать минимальное расстояние до точки
                    if (offset < result.offsetAbs) {

                        // Запомнить лучший результат
                        result.offsetAbs = offset
                        result.offsetRight = deltaAzimuth < 180
                        result.meter = meter
                        result.crossPoint = pointOnPolyline
                        result.segmentNum = segmentNum
                    }
                }

                // Если перпендикуляр к линии не на отрезке — протестировать границы отрезка
                when {
                    subLen <= 0.0 -> checkForMin(a, accumulatedLength, i-1, distancePointToA)
                    subLen >= sectionLen -> {
                        // Берём геодезическое расстояние на поверхности между начальной точкой и заданной
                        val distancePointToB = pointDistances[i+1 - beginIndex]
                        checkForMin(b, accumulatedLength + sectionLen, i+1, distancePointToB)
                    }
                    else -> {
                        // Вычислим координату пересечения перпендикуляра
                        val gd: GeodesicData = Geodesic.WGS84.Direct(
                            a.y, a.x,
                            sectionAzimuth, subLen
                        )
                        val pointOnSec = Coordinate(gd.lon2, gd.lat2)

                        // Рассчитать метрическое расстояние от найденной точки до заданной
                        val gd2: GeodesicData = Geodesic.WGS84.Inverse(
                            pointOnSec.y, pointOnSec.x,
                            point.y, point.x
                        )
                        val offset = gd2.s12

                        val pointOnSection = Coordinate(pointOnSec.x, pointOnSec.y)
                        checkForMin(pointOnSection, accumulatedLength + subLen, i, offset)
                    }
                }
            }
        }

        return result
    }

    /**
     * Рассчитать координату вдоль ломаной, зная смещение вдоль оси и в сторону от оси.
     * Базовый алгоритм для вычисления координаты точки из км+ смещений.
     * @param shift - смещение вдоль оси дороги в метрах
     * @param offset - перпендикулярное расстояние от оси до искомой точки
     * @return - координата искомой точки
     */
    fun calcLocationFromShiftAndOffset(shift: Double, offset: Double): Coordinate? {
        // Заведомо неразрешимые смещения откинуть
        if (shift < 0 || shift > length) return null

        // Найдём сегмент, внутри которого находится искомая точка перпендикуляра
        var restOfTheWay = shift    // Оставшийся путь
        var segmentIndex = 0        // Индекс сегмента, в котором искомая точка
        // (или номер точки ломаной, после которой искомая)
        while (segmentIndex < segmentDistances.size && restOfTheWay > segmentDistances[segmentIndex]) {
            // Пока есть куда идти (есть далее сегмент и его длина меньше оставшегося пути):
            // Идём вперёд: на следующий сегмент и уменьшаем оставшийся путь
            restOfTheWay -= segmentDistances[segmentIndex]
            segmentIndex++
        }

        // Установим опорную точку и направление найденного сегмента
        // Получим точку на ломаной, она будет являться опорной для следующего шага
        val gd: GeodesicData = Geodesic.WGS84.Direct(
            allPoints[segmentIndex].y, allPoints[segmentIndex].x,
            segmentAzimuths[segmentIndex], restOfTheWay
        )
        val pointOnLine = Coordinate(gd.lon2, gd.lat2)

        // От найденной точки зададим перпендикуляр в сторону знака offset
        val gd2: GeodesicData = Geodesic.WGS84.Direct(
            pointOnLine.y, pointOnLine.x,
            segmentAzimuths[segmentIndex] + 90.0, offset
        )

        return Coordinate(gd2.lon2, gd2.lat2)
    }

    /**
     * Класс для описания результата поиска
     * Ломанная+координата => смещение от начала, смещение от оси, точка пересечения с ломанной&
     * По умолчанию заданы пессимистичные значения «не найдено».
     */
    class ShiftAndOffset {
        // Расстояние от оси, м
        var offsetAbs: Double = Double.MAX_VALUE
        // Направление смещения от оси, FALSE=влево, TRUE=вправо
        var offsetRight: Boolean = true
        // Смещение от начала ломаной, м
        var meter: Double? = null
        // Точка пересечения перпендикуляра от заданной точки к ломаной
        var crossPoint: Coordinate? = null
        // Номер сегмента ломаной, содержащей пересечение с перпендикуляром
        // (нумерация с нуля)
        var segmentNum: Int = -1

        // Отладочная функция для вывода содержимого объекта в консоль
        override fun toString() = "{closestPoint: [${crossPoint?.x ?: '?'}, ${crossPoint?.y ?: '?'}], "+
                "closestPlus: ${meter ?: '–'}, offset: ${if (offsetRight) "→" else "←"} $offsetAbs, segmentNum: $segmentNum}"
    }

    /**
     * Вычислить примерный расход памяти в байтах.
     */
    fun memorySize(): Long {
        return allPoints.size * 3 * 4L + /* Coordinate ~ 3 x Double */
            segmentDistances.size * 4L +
                segmentAzimuths.size * 4L +
                accumulatedDistances.size * 4L +
                4 * 4L + 4L /* envelope + length */
    }

    companion object {

        /**
         * Создать калькулятор, опираясь на геометрию ломаной.
         * @param axis - ломаная
         * @return - Калькулятор
         */
        fun createFromLineString(axis: Array<Coordinate>): RoadSegment {
            var minX: Double = Double.MAX_VALUE
            var minY: Double = Double.MAX_VALUE
            var maxX: Double = Double.MIN_VALUE
            var maxY: Double = Double.MIN_VALUE

            val distances = DoubleArray(axis.size-1)
            val azimuths = DoubleArray(axis.size-1)
            val accumulatedDistances = DoubleArray(axis.size-1)
            var accumulatedDistance = 0.0

            // Функция проверки и обновления границ
            fun checkEnvelope(p: Coordinate) {
                if (minX > p.x) minX = p.x
                if (minY > p.y) minY = p.y
                if (maxX < p.x) maxX = p.x
                if (maxY < p.y) maxY = p.y
            }

            // По циклу пройти все отрезки, рассчитывая дистанцию
            for (i in 0 until axis.size-1) {
                // Начальная и конечная точки исследуемого сегмента
                val a = axis[i]
                val b = axis[i+1]

                // Обновим границы координат
                checkEnvelope(a)
                checkEnvelope(b)

                // Рассчитаем геодезическое расстояние на поверхности между начальной точкой и заданной
                val gd: GeodesicData = Geodesic.WGS84.Inverse(
                    a.y, a.x,
                    b.y, b.x
                )
                val abLen = gd.s12
                val abAzimuth = gd.azi1

                distances[i] = abLen
                azimuths[i] = abAzimuth
                accumulatedDistances[i] = accumulatedDistance
                accumulatedDistance += abLen
            }

            return RoadSegment(
                allPoints = axis,
                segmentDistances = distances.toTypedArray(),
                segmentAzimuths = azimuths.toTypedArray(),
                accumulatedDistances = accumulatedDistances.toTypedArray(),
                envelope = Envelope(minX, minY, maxX, maxY),
                length = accumulatedDistance
            )
        }

    }

}