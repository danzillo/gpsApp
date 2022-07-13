package com.example.myapplication3.location.kmpluscalc.kmpluscalculator

import com.example.myapplication3.location.kmpluscalc.data.*
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

/**
 * Класс для работы с осью дороги.
 * Важные предположения и оговорки:
 * — исключается дублирование номеров километровых столбов.
 */
class RoadKmPlusAxis private constructor(
    private val roadKmSegments: List<RoadKmSegment>
) {

    /**
     * Поиск и расчёт км+ из точки.
     * @param longitude - долгота
     * @param latitude - широта
     * @return - структура с описанием результата (км, метры, смещение от оси)
     */
    fun calcKmPlusFromLocation(longitude: Double, latitude: Double): KmPlusOffset? {
        return calcKmPlusFromLocation( Coordinate(longitude, latitude) )
    }
    fun calcKmPlusFromLocation(point: Coordinate): KmPlusOffset? {
        return searchSegmentAndKmPlusFromLocation(point)?.second
    }

    /**
     * Поиск и расчёт проектного километража (км,ммм) из координаты.
     */
    fun calcKmDoubleFromLocation(point: Coordinate): KmDoubleOffset? {
        val segmentAndKmPlus = searchSegmentAndKmPlusFromLocation(point)
        val segment: RoadKmSegment? = segmentAndKmPlus?.first
        val offset: KmPlusOffset? = segmentAndKmPlus?.second

        return if (segment != null && offset != null) {
            KmDoubleOffset(
                km = segment.startKmDouble + offset.meter / 1000.0,
                offset = offset.offset,
                crossPoint = offset.crossPoint
            )
        } else null
    }

    /**
     * Получение длины километрового участка.
     * @param km - километр
     */
    fun getKmSegmentLength(km: Int): Double? {
        return getKmSegment(km)?.length
    }

    /**
     * Нахождение координаты для КМ+ и отклонения.
     * @param kmPlusOffset - эксплуатационная точка КМ+
     * @return - координата (если получилось рассчитать)
     */
    fun calcLocationFromKmPlus(kmPlusOffset: KmPlusOffset): Coordinate? {
        if (kmPlusOffset.km == null) return null

        val newKmPlusOffset: KmPlusOffset = normalizeKmPlusOffset(kmPlusOffset) ?: return null

        val kmSegment: RoadKmSegment? = getKmSegment(newKmPlusOffset.km!!)
        //log.info("calcLocationFromKmPlus: kmSegment = $kmSegment")

        val coordinate: Coordinate? = kmSegment?.calcLocationFromKmPlus(newKmPlusOffset)
        //log.info("calcLocationFromKmPlus: coordinate = $coordinate")

        return coordinate
    }

    /**
     * Нахождение координаты для КМ,ммм и отклонения.
     */
    fun calcLocationFromKmDouble(km: Double, offset: Double): Coordinate? {
        // Найдём сегмент, содержащий искомое смещение
        val foundSegment: RoadKmSegment? = roadKmSegments.singleOrNull { segment ->
            segment.startKmDouble <= km &&
                    segment.startKmDouble + segment.length / 1000.0 > km
        }
        //log.info("calcLocationFromKmDouble: foundSegment = $foundSegment")

        // Выход, если не найден
        if (foundSegment == null) return null

        // Внутри найденного сегмента дороги ищем координату
        val coordinate: Coordinate? = foundSegment.calcLocationFromKmDouble(
            km = km,
            offset = offset
        )
        //log.info("calcLocationFromKmDouble: coordinate = $coordinate")

        return coordinate
    }

    /**
     * Нормализация КМ+М пикетажа. Требуется уточнить КМ при минимально положительном М.
     * @param kmPlusOffset - КМ+М, которые следует проверить
     * @return - уточнённый КМ+М
     */
    private fun normalizeKmPlusOffset(kmPlusOffset: KmPlusOffset): KmPlusOffset? {
        //TODO: Покрыть код тестами

        // Попытаться нормализовать КМ+М (уточнить КМ при минимально положительном М)
        var newKmPlus: KmPlusOffset = kmPlusOffset
        // Получим километровый сегмент, указанный во входных параметрах
        var kmSegment: RoadKmSegment = getKmSegment(kmPlusOffset.km!!) ?: return null
        // Учесть случай с КМ+999999м (переход на следующие км-участки)
        while (newKmPlus.meter > kmSegment.length) {
            // Запрошено метровое смещение вдоль дороги превышает длину участка между км-столбами.
            //log.info("normalizeKmPlusOffset: KM-Segment to short for ${newKmPlus.km!!}+${newKmPlus.meter} → ${newKmPlus.km!! + 1}+${newKmPlus.meter - kmSegment.length}")

            // Обновить КМ+М для повторной проверки нового км-сегмента
            newKmPlus = KmPlusOffset(
                km = newKmPlus.km!! + 1,
                meter = newKmPlus.meter - kmSegment.length,
                offset = newKmPlus.offset,
                crossPoint = newKmPlus.crossPoint
            )
            // Нужно переходить на следующий км-сегмент.
            kmSegment = getKmSegment(newKmPlus.km!!) ?: return null
        }

        // Учесть случай с КМ-метры (переход на предыдущие км-участки)
        // При отрицательном метровом смещении - рассматриваем предыдущий км-участок
        while (newKmPlus.meter < 0) {
            // Предыдущий км-участок
            kmSegment = getKmSegment(newKmPlus.km!! - 1) ?: return null
            // Получим его длину, м
            val segmentLen: Double = kmSegment.length

            //log.info("normalizeKmPlusOffset: Meter is lower zero ${newKmPlus.km!!}-${-newKmPlus.meter} → ${newKmPlus.km!! - 1}+${segmentLen + newKmPlus.meter}")

            // Попробуем рассмотреть новый вариант КМ+М
            newKmPlus = KmPlusOffset(
                km = newKmPlus.km!! - 1,
                meter = segmentLen + newKmPlus.meter,
                offset = newKmPlus.offset,
                crossPoint = newKmPlus.crossPoint
            )
        }

        return newKmPlus
    }

    private fun searchSegmentAndKmPlusFromLocation(point: Coordinate): Pair<RoadKmSegment, KmPlusOffset>? {
        val accuracy = 0.05     // Заданная точность поиска, м

        // Построить вокруг координаты (в градусах) прямоугольник-окрестность (в градусах).
        // ± 0.5 минута по вертикали   ~ это 1.8 км в высоту
        // ± 1.0 минута по горизонтали ~ это на широте Казани примерно 2.2 км в высоту
        val pointEnvironment = Envelope(point.x - 0.5,  point.y - 1.0, point.x + 0.5, point.y + 1.0)

        // Выбрать из всех километровых сегментов которые коснулись окрестности точки
        val nearestSegments: List<RoadKmSegment> =
            roadKmSegments.filter { segment ->
                segment.envelope.intersects(pointEnvironment)
            }
        // К каждому из найденных сегментов задать вопрос «найти перпендикуляр»
        val perpendiculars: List<Pair<RoadKmSegment, KmPlusOffset>> =
            nearestSegments
                .map { segment ->
                    Pair(segment, segment.calcKmPlusFromLocation(point))
                }
                // Оставить только сегменты, по которым удалось рассчитать перпендикуляр
                .filter { segmentAndShift ->
                    segmentAndShift.second != null
                }
                .map { segmentAndShift ->
                    Pair(segmentAndShift.first, segmentAndShift.second!!)
                }

        // Ближайший математический не всегда практически удобен для использования.
        // Особенный случай: 0+999,9 за сантиметр до следующего столба.
        // На практике, в поле инженер укажет 1+000.
        // Значит, ищем минимум, а около него смотрим наличие значимой вехи — следующий столб.

        // 1) Найти из всех ответов только ближайший
        val nearestSegmentAndPerpendicular: Pair<RoadKmSegment, KmPlusOffset> =
            perpendiculars.minByOrNull { segmentAndPerpendicular ->
                segmentAndPerpendicular.second.offset.absoluteValue
            } ?: return null    // Если Null - вернём null и хватит
        //log.info("RoadPath: Found minimum offset: ${nearestSegmentAndPerpendicular.second.offset.absoluteValue}, for segment ${nearestSegmentAndPerpendicular.first.startKmPlus}")

        // 2) Выбираем ближайшие в окрестности
        val nearestSegmentAndPerpendicularAround: List<Pair<RoadKmSegment, KmPlusOffset>> =
            perpendiculars.filter { segmentAndPerpendicular ->
                (segmentAndPerpendicular.second.offset.absoluteValue -
                        nearestSegmentAndPerpendicular.second.offset.absoluteValue).absoluteValue < accuracy
        }.onEach { segmentAndPerpendicular ->
            //log.info("RoadPath: Nearest segment:  ${segmentAndPerpendicular.first.startKmPlus}")
        }

        // 3) Отдать предпочтение большему КМ-столбу :(
        return if (nearestSegmentAndPerpendicularAround.size == 1) {
            //log.info("RoadPath: alone variant")
            nearestSegmentAndPerpendicular
        } else {
            //log.info("RoadPath: selecting strongest variant")
            nearestSegmentAndPerpendicularAround
                .maxByOrNull { segmentAndPerpendicular ->
                    segmentAndPerpendicular.first.startKmPlus.km ?: Int.MIN_VALUE
                }
        }
    }

    /**
     * Получения нужного километрового участка.
     * @param km - километр
     * @return - сегмент оси до следующего КМ-столба или до конца оси
     */
    private fun getKmSegment(km: Int): RoadKmSegment? {
        //log.info("getKmSegment(km = $km)")
        //log.info("getKmSegment: roadKmSegments.size = ${roadKmSegments.size}")
        return roadKmSegments.firstOrNull { segment ->
            segment.startKmPlus.km == km
        }
    }

    /**
     * Вычислить примерный расход памяти в байтах.
     */
    fun memorySize(): Long {
        val segmentSizes: List<Long> = roadKmSegments.map { it.memorySize() }
        return segmentSizes.sum()
    }

    companion object {

        /**
         * Создание направления дороги из метрики начала, оси и столбов.
         * @param startKmPlus - эксплуатационный пикет начала дороги (КМ+)
         * @param axis - ломанная линия проектной оси дороги
         * @param kmPoints - связанная коллекция, где ключ - К-столб, значение - координата
         * @return - калькулятор для указанной проектной оси дороги
         */
        fun create(
            startKmDouble: Double,
            startKmPlus: KmPlus,
            axis: Array<Coordinate>,
            kmPoints: Map<Int, Coordinate>
        ): RoadKmPlusAxis {
            // Накопитель для километровых участков
            val segmentsBuf = mutableListOf<RoadKmSegment>()

            // Разбиение оси дороги на километровые сегменты.
            // Сформируем механизм поиска перпендикуляров
            val fullLinePerpendicularSearcher = RoadSegment.createFromLineString(axis)

            // 1)
            // Для каждого км столба найти точку пересечения перпендикуляра с осью дороги.
            // Каждая такая точка в последующем станет разделителем смежных сегментов.
            // Также надо запомнить номер участка ломаной, где была найдена точка.
            // Каждый элемент: (№ точки ломаной перед пересечением, км столб №, результат расчёта)
            val kmCrosses = mutableListOf<Triple<Int, Int, RoadSegment.ShiftAndOffset>>()
            val calcTime = measureTimeMillis {
                kmPoints.forEach { (km, point) ->
                    val perpendicular = fullLinePerpendicularSearcher.calcShiftAndOffsetFromLocation(point)
                    // Запоминать точки только разделяющие ось, выкидывать которые выпали за границы
                    if (perpendicular.segmentNum >= 0 && perpendicular.segmentNum < axis.size-1 && perpendicular.crossPoint != null) {
                        kmCrosses.add(Triple(perpendicular.segmentNum, km, perpendicular))
                    }
                }
            }
            //log.info("RoadPath.Create: Search perpendicular time, ms (Target < 10) = ${calcTime/kmPoints.size}, total,ms = $calcTime, for ${kmPoints.size} km points")

            // Сортировать по порядку вдоль оси дороги
            kmCrosses.sortBy { it.first /* по номерам точек оси */ }

            // 2)
            // Разделить всю ось дороги на километровые сегменты
            // Пикетаж начальной точки сегмента:
            var segmentKmPlus = startKmPlus
            // Десятичный километраж начальной точки сегмента
            var segmentKmDouble = startKmDouble
            // Буфер точек сегмента:
            var segmentPoints = mutableListOf<Coordinate>()
            // Индекс следующей точки пересечения с КМ столбом
            var nextKmIndex = 0
            // Номер точки на оси, перед которой найдено пересечение с КМ столбом
            var segmentStopIndex =
                if (kmCrosses.isNotEmpty())
                    kmCrosses[nextKmIndex].first    /* номер точки, после которой надо разделить ломаную */
                else
                    axis.size                       /* точек пересечения нет - вся ось единый сегмент */

            for (i in axis.indices) {

                // Если актуальная точка до пересечения - просто добавить в буфер
                if (i <= segmentStopIndex) {
                    segmentPoints.add(axis[i])
                } else {
                    // Актуальная точка после пересечения!
                    // Добавим в буфер именно точку пересечения
                    segmentPoints.add(kmCrosses[nextKmIndex].third.crossPoint!!)

                    // Формирование сегмента завершили - сохранить сегмент
                    val roadKmSegment = RoadKmSegment(
                        startKmDouble = segmentKmDouble,
                        startKmPlus = segmentKmPlus,
                        axis = segmentPoints.toTypedArray()
                    )
                    segmentsBuf.add(roadKmSegment)

                    // Увеличим километраж для последующего сегмента
                    segmentKmDouble += roadKmSegment.length / 1000.0
                    // Обновить пикетаж начала сегмента на следующую позицию
                    segmentKmPlus = KmPlus(
                        kmCrosses[nextKmIndex].second /* km */, 0.0)
                    // Начать новый буфер с точки пересечения и актуальной точки
                    segmentPoints = mutableListOf(
                        kmCrosses[nextKmIndex].third.crossPoint!!,
                        axis[i]
                    )

                    // Увеличить индекс следующей точки пересечения
                    nextKmIndex++
                    // Обновить номер точки, перед которой имеется пересечение
                    segmentStopIndex =
                        if (kmCrosses.size > nextKmIndex)
                            kmCrosses[nextKmIndex].first    /* номер точки, после которой надо разделить ломаную */
                        else
                            axis.size         /* точек пересечения более нет - остальная ось единый сегмент */
                }

            }
            // Всегда в буфере остаётся хвостик — финальный кусок ломаной.
            // Добавить его в список участков
            segmentsBuf.add(
                RoadKmSegment(
                    startKmDouble = segmentKmDouble,
                    startKmPlus = segmentKmPlus,
                    axis = segmentPoints.toTypedArray()
                )
            )

            //log.info("RoadPath.Create: Segment count = ${segmentsBuf.size}")

            return RoadKmPlusAxis(segmentsBuf.toList())
        }

    }

}