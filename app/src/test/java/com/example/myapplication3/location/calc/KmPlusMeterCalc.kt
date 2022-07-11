package com.example.myapplication3.location.calc

class KmPlusMeterCalc {

    fun checkKmPluM(
        crossPoints: MutableList<Coordinate>,
        myPos: Coordinate,
        segments: MutableList<SegmentData>
    ): KmPlusMeter {
        // Для определения ближайшего столба
        val r1 = ShiftAndOffsetCalc().shiftAndOffsetCalc(crossPoints, myPos)
        // Если координата на краю оси
        println("R1:" + r1.minPoint)
        println("R1:" + r1.shift)
        if (r1.minPoint == crossPoints.lastIndex && r1.isAheadPoint) {
            r1.minPoint -= 1
            val r2 = ShiftAndOffsetCalc().shiftAndOffsetCalc(segments[r1.minPoint].segment, myPos)
            println(r2.totalLength)
            //  println("R1:"+r1.minPoint)
            //  println("R1:"+r1.shift)
            return KmPlusMeter(r1.minPoint, r2.shift, r2.offset)
        }

        if (r1.minPoint > 0 && r1.shift < segments[r1.minPoint - 1].length)
            r1.minPoint -= 1
        // TODO Слепой, comment
        val r2 = ShiftAndOffsetCalc().shiftAndOffsetCalc(segments[r1.minPoint].segment, myPos)

        return KmPlusMeter(r1.minPoint, r2.shift, r2.offset)
    }
}