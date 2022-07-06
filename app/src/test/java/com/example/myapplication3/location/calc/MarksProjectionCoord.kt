package com.example.myapplication3.location.calc

class MarksProjectionCoord(
    var latitude: Double,
    var longitude: Double,
    var length: Double,
    var offset: Double
)

val marksProjectionCoord = mutableMapOf<Int, MarksProjectionCoord>()
