package com.example.myapplication3.location.calc

class KmPlusMeterCalc(
    val crossPoints: MutableList<Coordinate>,
    val myPos: Coordinate,
    val segments: MutableList<SegmentData>
) {
    //  val locationData: KmPlusMeter? = null

    // Для определения ближайшего столба
    private val r1 = ShiftAndOffsetCalc(crossPoints, myPos).shiftAndOffsetCalc()

    fun checkKmPluM(): KmPlusMeter {

        // Если координата на краю оси
        println(r1.minPoint)
        println(r1.shift)
        if (r1.minPoint == crossPoints.lastIndex && r1.isAheadPoint) {
            r1.minPoint -= 1
            val r2 = ShiftAndOffsetCalc(segments[r1.minPoint].segment, myPos).shiftAndOffsetCalc()
            println(r2.totalLength)
            return KmPlusMeter(r1.minPoint, r2.shift, r2.offset)
        }

        if (r1.minPoint > 0 && r1.minPoint <= segments.lastIndex && r1.shift < segments[r1.minPoint].length)
            r1.minPoint -= 1

        val r2 = ShiftAndOffsetCalc(segments[r1.minPoint].segment, myPos).shiftAndOffsetCalc()

        return KmPlusMeter(r1.minPoint, r2.shift, r2.offset)
    }
}