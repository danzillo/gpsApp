package com.example.myapplication3.location.calc

class KmPlusMeterCalc(
    val crossPoints: MutableList<Coordinate>,
    val myPos: Coordinate,
    val segmentt: MutableList<SegmentData>
) {
    // Для определения ближайшего столба
    val r1 = ShiftAndOffsetCalc(crossPoints, myPos)
    fun checkKmPluM() {
        if (r1.minPoint == crossPoints.lastIndex && r1.isAheadPoint) {
            r1.minPoint -= 1

            // Вместо кросс поинтс должен быть сегмент
            val r2 = ShiftAndOffsetCalc(segmentt[r1.minPoint], myPos)
            println(r2.totalLength)
        }
    }