package com.example.myapplication3.location.calc

data class KilometerSegment(
    val segment: MutableList<Coordinate>,
    val kmLength: Double,
    val kmPoints: MutableList<Coordinate>,
)

class PointData(
    val km: Int,
    val meter: Double,
    val offset: Double
)

fun roadKilometerSegment(
    axis: MutableList<Coordinate>,
    kmPoint: MutableList<Coordinate>,
    cord: Coordinate
): MutableList<PointData> {
    val roadKilometerMap = mutableMapOf<Int, KilometerSegment>()
    val road = mutableListOf<PointData>()
    var kmLength: Double
    val prevPoint = 0
    var nextPoint = 0
    var prevCrossPoint = Coordinate(0.0, 0.0)
    val lastPoint = axis.lastIndex
    val kmPoints = mutableListOf<Coordinate>()
    val kmPointsLength = mutableListOf<Double>()
    var totalLength: Double = 0.0

    for (kmPointCounter in 0..kmPoint.lastIndex) {
        val segment = mutableListOf<Coordinate>()
        // Находим координаты проекции столба, ближайшую вершину(слева//справа от столба)
        // смещение и расстояние до проекции
        val kmShiftAndOffset: ShiftAndOffset = shiftAndOffsetCalc(
            axis,
            kmPoint[kmPointCounter]
        )
        // Записываем полную длину сегмента
        totalLength = kmShiftAndOffset.totalLength

        // Записываем длину участка
        kmPointsLength.add(kmShiftAndOffset.shift)

        // Сохраняем все вершины для участка дороги между км столбами

        // Заполняем сегмент до 1 столба
        if (kmPointCounter == 0) {
            // Записываем в kmPoints начальную точку оси
            kmPoints.add(axis[prevPoint])
            for (axisCounter in prevPoint..kmShiftAndOffset.prevPoint) {
                segment.add(axis[axisCounter])
            }
            // Записываем координаты проекции в kmPoints
            kmPoints.add((axis[kmShiftAndOffset.prevPoint]))
        }
        // Заполняем сегменты с 1 до 2 и со 2 до 3 столба
        else {
            // Записываем координаты проекции
            kmPoints.add(kmShiftAndOffset.crossPoint)
            // Добавляем к сегменту точку пересечения столба сзади
            segment.add(prevCrossPoint)

            for (axisCounter in nextPoint..kmShiftAndOffset.prevPoint) {
                segment.add(axis[axisCounter])
            }
        }

        // Добавляем точку пересечения с перпендикуляром от км столба
        segment.add(kmShiftAndOffset.crossPoint)

        if (kmPointCounter == kmPoint.lastIndex) {
            segment.add(kmShiftAndOffset.crossPoint)
            for (axisCounter in nextPoint..kmShiftAndOffset.prevPoint) {
                segment.add(axis[axisCounter])
            }
            segment.add(kmShiftAndOffset.crossPoint)
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

    // Находим ближайший столбец к точке
    val r1 = shiftAndOffsetCalc(kmPoints, cord)

    // Добавляем расстояние до 0 точки, и до конца оси(полное расстояние)
    kmPointsLength.add(0, 0.0)
    kmPointsLength.add(kmPointsLength.size, totalLength)

    // Учитываем с какой стороны относительно точки находится столб
    if (r1.minPoint > 0 && r1.minPoint <= kmPointsLength.lastIndex && r1.shift < kmPointsLength[r1.minPoint])
        r1.minPoint -= 1

/*    println(roadKilometerMap[3]?.segment!![23].latitude)
    println(roadKilometerMap[3]?.segment!![23].longitude)*/
    val r2 = shiftAndOffsetCalc(roadKilometerMap[r1.minPoint]?.segment!!, cord)
    road.add(PointData(r1.minPoint, r2.shift, r2.offset))
    //val shift = r2.shift


    return road
}