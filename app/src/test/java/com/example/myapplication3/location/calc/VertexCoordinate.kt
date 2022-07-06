package com.example.myapplication3.location.calc

class VertexCoordinate(
    var longitude: Double,
    var latitude: Double,
    var azimuthNext: Double,
    var azimuthPrev: Double
)

//List
val vertexCoordinate = mutableMapOf<Int, VertexCoordinate>()
