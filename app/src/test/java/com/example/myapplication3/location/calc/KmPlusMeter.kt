package com.example.myapplication3.location.calc

class KmPlusMeter()

fun kmPlusMeter(
    kilometer: MutableMap<Int, KilometerSegment>,
    coordinate: Coordinate
) {
    println(kilometer[0]?.kmPoints?.get(1))
    val list = kilometer[0]?.kmPoints
    val km = shiftAndOffsetCalc(list!!,coordinate)
    if (km != null) {
        println("КМ: ${km.prevPoint}")
    }

}