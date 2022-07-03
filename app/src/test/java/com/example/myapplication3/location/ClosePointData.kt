package com.example.myapplication3.location

class ClosePointData(
    var longitude: Double,
    var latitude: Double,
    var azimuthNext: Double,
    var azimuthPrev: Double
)

//List
val closeCoordToColumnMap = mutableMapOf<Int, ClosePointData>()
