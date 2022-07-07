package com.example.myapplication3.location.calc

class KilometerSegment(
    val segment: MutableList<Coordinate>,
    val kmLength: Double,
    val kmPoints: MutableList<Coordinate>
)

fun roadKilometerSegment(
    axis: MutableList<Coordinate>,
    kmPoint: MutableList<Coordinate>
): MutableMap<Int, KilometerSegment> {
    val roadKilometerMap = mutableMapOf<Int, KilometerSegment>()
    var kmLength: Double
    val prevPoint = 0
    var nextPoint = 0
    var prevCrossPoint = Coordinate(0.0, 0.0)
    val lastPoint = axis.lastIndex
    val kmPoints = mutableListOf<Coordinate>()

    for (kmPointCounter in 0..kmPoint.lastIndex) {
        val segment = mutableListOf<Coordinate>()
        // Находим координаты проекции столба, ближайшую вершину(слева//справа от столба)
        // смещение и расстояние до проекции
        val kmShiftAndOffset: ShiftAndOffset = shiftAndOffsetCalc(
            axis,
            kmPoint[kmPointCounter]
        )

        // Сохраняем все вершины для участка дороги между км столбами
        if (kmPointCounter == 0) {
            kmPoints.add(axis[prevPoint])
            for (axisCounter in prevPoint..kmShiftAndOffset.prevPoint) {
                segment.add(axis[axisCounter])
            }
        } else {
            kmPoints.add(kmShiftAndOffset.crossPoint)
            segment.add(kmShiftAndOffset.crossPoint)
            for (axisCounter in nextPoint..kmShiftAndOffset.prevPoint) {
                segment.add(prevCrossPoint)
            }
        }
        //kmPoints.add(kmShiftAndOffset.crossPoint)
        // Добавляем точку пересечения с перпендикуляром от км столба
        segment.add(kmShiftAndOffset.crossPoint)

        if (nextPoint < lastPoint && kmPointCounter == kmPoint.lastIndex) {
            segment.add(kmShiftAndOffset.crossPoint)
            for (axisCounter in nextPoint..lastPoint) {
                segment.add(axis[axisCounter])
            }
            kmPoints.add(axis[lastPoint])
            roadKilometerMap[kmPointCounter + 1] = KilometerSegment(segment, 0.0, kmPoints)
        }

        // Расстояние от начала до проекции
        kmLength = kmShiftAndOffset.shift

        // Точка пересечения перпендикуляра с дорогой (с нее заполняется сегмент)
        prevCrossPoint = kmShiftAndOffset.crossPoint

        // Точка, которая будет записываться после prevCrossPoint
        nextPoint = kmShiftAndOffset.nextPoint

        roadKilometerMap[kmPointCounter] = KilometerSegment(segment, kmLength, kmPoints)
    }

    return roadKilometerMap
}