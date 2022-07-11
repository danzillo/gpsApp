package com.example.myapplication3.location.calc

class KmPlusMeterCalc {
    /**
     * @param crossPoints точки пересечение КМ столбов с дорогой
     * @param myPos позиция человека
     * @param segments информация о сегментах дороги
     * @return КМ+М+Offset
     */
    fun checkKmPluM(
        crossPoints: MutableList<Coordinate>,
        myPos: Coordinate,
        segments: MutableList<SegmentData>
    ): KmPlusMeter {

        // Находит ближайший к точке столб
        val r1 = ShiftAndOffsetCalc().shiftAndOffsetCalc(crossPoints, myPos)

        // Если координата точки на краю оси
        if (r1.minPoint == crossPoints.lastIndex && r1.isAheadPoint) {

            // Определяем ближайший км столб
            r1.minPoint -= 1

            // Ищем ShiftAndOffset на сегменте в пределах которого находится точка
            val r2 = ShiftAndOffsetCalc().shiftAndOffsetCalc(segments[r1.minPoint].segment, myPos)

            return KmPlusMeter(r1.minPoint, segments[segments.lastIndex].length - segments[segments.lastIndex-1].length , r2.offset)
        }

        // Если координата точки вблизи вершины, которая находится за точкой
        if (r1.minPoint > 0 && r1.shift < segments[r1.minPoint - 1].length)
            r1.minPoint -= 1

        // Ищем ShiftAndOffset на сегменте в пределах которого находится точка
        val r2 = ShiftAndOffsetCalc().shiftAndOffsetCalc(segments[r1.minPoint].segment, myPos)

        return KmPlusMeter(r1.minPoint, r2.shift, r2.offset)
    }
}