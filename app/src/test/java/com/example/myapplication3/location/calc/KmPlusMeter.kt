package com.example.myapplication3.location.calc

class KmPlusMeter()

fun kmPlusMeter(
    kilometer: MutableMap<Int, KilometerSegment>
) {
    for (counter in 0 .. kilometer[0]?.kmPoints?.lastIndex!!)

    println("КМ: "+ (kilometer[0]?.kmPoints?.get(0)?.latitude))

}