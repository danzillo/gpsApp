package com.example.myapplication3.location.calc

class KmSegmentCalc(
    val kmData: MutableList<ShiftAndOffset>,
    val numOfPoints: Int,
) {
    //val kmkmkm = KilometerPointsCalc(axis, distanceMarks)
   // val roadKilometerMap = mutableMapOf<Int, KilometerSegment>()
  //  val road = mutableListOf<PointData>()
    var kmLength: Double = 0.0
    val prevPoint = 0
    var nextPoint = 0
  //  var prevCrossPoint = Coordinate(0.0, 0.0)
    val lastPoint = axis.lastIndex
    val kmCrossPoints = mutableListOf<Coordinate>()
    val kmPointsLength = mutableListOf<Double>()
    var totalLength: Double = 0.0
    val kmShiftAndOffset = mutableListOf<ShiftAndOffset>()



}