package com.example.myapplication3.location.kmpluscalc.kmpluscalculator

import com.example.myapplication3.location.kmpluscalc.data.*

/**
 * Класс для сегмента дороги между километровыми столбами.
 */
class RoadKmSegment(
    val startKmDouble: Double,
    val startKmPlus: KmPlus,
    axis: Array<Coordinate>
) {
    //IDEA: Ещё одна оптимизация по скорости — хранить для каждой точки метрику км+
    // но будут издержки по памяти.

    private val searcher: RoadSegment = RoadSegment.createFromLineString(axis)

    // Охват координат ломаной
    val envelope: Envelope = searcher.envelope
    // Длина ломанной в метрах
    val length: Double = searcher.length

    /**
     * Получить координату по заданному КМ+ и смещению от оси.
     */
    fun calcLocationFromKmPlus(kmPlusOffset: KmPlusOffset): Coordinate? {
        return if (kmPlusOffset.km == startKmPlus.km)
            searcher.calcLocationFromShiftAndOffset(kmPlusOffset.meter - startKmPlus.meter, kmPlusOffset.offset)
        else
            null
    }

    /**
     * Получить координату по заданному КМ,М и смещению от оси.
     * @param km смещение вдоль оси в километрах
     * @param offset перпендикуляр (плюс - направо от оси)
     * @return координата
     */
    fun calcLocationFromKmDouble(km: Double, offset: Double): Coordinate? {
        return searcher.calcLocationFromShiftAndOffset(
            shift = (km - startKmDouble) * 1000.0,
            offset = offset)
    }

    fun calcKmPlusFromLocation(coordinate: Coordinate): KmPlusOffset? {
        val shiftAndOffset: RoadSegment.ShiftAndOffset = searcher.calcShiftAndOffsetFromLocation(coordinate)
        return if (shiftAndOffset.meter != null) {
            KmPlusOffset(
                km = startKmPlus.km,
                meter = shiftAndOffset.meter!! + startKmPlus.meter,
                offset = if(shiftAndOffset.offsetRight) shiftAndOffset.offsetAbs else -shiftAndOffset.offsetAbs,
                crossPoint = shiftAndOffset.crossPoint
            )
        } else {
            null
        }
    }

    /**
     * Получить КМ,М по заданной координате.
     * @param coordinate координата точки
     * @return положение точки в КМ,М
     */
    fun calcKmDoubleFromLocation(coordinate: Coordinate): KmDoubleOffset? {
        val shiftAndOffset: RoadSegment.ShiftAndOffset = searcher.calcShiftAndOffsetFromLocation(coordinate)
        return if (shiftAndOffset.meter != null) {
            KmDoubleOffset(
                km = startKmDouble + shiftAndOffset.meter!! / 1000.0,
                offset = if(shiftAndOffset.offsetRight) shiftAndOffset.offsetAbs else -shiftAndOffset.offsetAbs,
                crossPoint = shiftAndOffset.crossPoint
            )
        } else null
    }

    /**
     * Вычислить примерный расход памяти в байтах.
     */
    fun memorySize(): Long {
        return searcher.memorySize() +
                startKmPlus.memorySize()
    }

    override fun toString(): String {
        return "<RoadKmSegment> { startKmPlus: $startKmPlus ... }"
    }
}
