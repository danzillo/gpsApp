package com.example.myapplication3.location.calc

class KilometerPointsCalc(
    private val axis: MutableList<Coordinate>,
    private val kmPoint: MutableList<Coordinate>,
) {

    //  val roadKilometerMap = mutableMapOf<Int, KilometerSegment>()
    // val road = mutableListOf<PointData>()
    var kmLength: Double = 0.0
    val prevPoint = 0
    var nextPoint = 0
    var prevCrossPoint = Coordinate(0.0, 0.0)
    val lastPoint = axis.lastIndex
    val kmCrossPoints = mutableListOf<Coordinate>()
    val kmPointsLength = mutableListOf<Double>()
    var totalLength: Double = 0.0
    val kmShiftAndOffset = mutableListOf<ShiftAndOffset>()
    val segmentData = mutableListOf<SegmentData>()

    // Поиск всех точек пересечения км столбов
    fun roadKilometerPoints(): MutableList<ShiftAndOffset> {
        /**
         * @param kmPointData - объект класса ShiftAndOffset для хранения информации о
         * сегментах дороги, а именно: длина от начала до проекции столба, местоположение столба относительно вершины,
         * точка пересечения столба с осью дороги.
         */
        // Сохраняем информацию по всем км столбам
        for (kmPointCounter in 0..kmPoint.lastIndex) {
            val kmPointData = ShiftAndOffsetCalc(axis, kmPoint[kmPointCounter])
            kmShiftAndOffset.add(kmPointData.shiftAndOffsetCalc())
        }
        return kmShiftAndOffset
    }

    // Разбитие оси на километровые сегменты
    fun kmSegments(): MutableList<SegmentData> {
        for (kmSegmentCounter in 0..kmPoint.lastIndex) {
            var segment = mutableListOf<Coordinate>()
            // Запись сегмент для 1 столба
            if (kmSegmentCounter == 0) {
                // Начальная точка оси
                kmCrossPoints.add(axis[prevPoint])
                // Записываем все точки между началом и 1 столбом
                println(kmShiftAndOffset[kmSegmentCounter].prevPoint)
                for (axisCounter in prevPoint..kmShiftAndOffset[kmSegmentCounter].prevPoint) {
                    segment.add(axis[axisCounter])
                }
                // Координаты проекции столба ??
                kmCrossPoints.add(kmShiftAndOffset[kmSegmentCounter].crossPoint)
            }

            // Запись сегменты от 1 до предпоследнего столба
            else {
                // Координаты проекции столба(2столб, 3столб и тд)
                kmCrossPoints.add(kmShiftAndOffset[kmSegmentCounter].crossPoint)

                // Сохраняем координаты начала нового сегмента
                segment.add(kmCrossPoints[kmSegmentCounter])

                // Сохраняем координаты вершин между проекциями столбов
                for (axisCounter in nextPoint..kmShiftAndOffset[kmSegmentCounter].prevPoint) {
                    segment.add(axis[axisCounter])
                }
            }

            // Добавляем точку в которой заканчивается данный сегмент
            segment.add(kmCrossPoints[kmSegmentCounter])

            // Индекс точки, которая идет после проекции столба на ось(2 в списке)
            nextPoint = kmShiftAndOffset[kmSegmentCounter].nextPoint

            // Сохраняем сегмент с его вершинами, длиной и номером
            if (kmSegmentCounter == 0) {
                segmentData.add(
                    SegmentData(
                        kmSegmentCounter,
                        segment,
                        kmShiftAndOffset[kmSegmentCounter].shift
                    )
                )
              //  println( kmShiftAndOffset[kmSegmentCounter].shift)
            } else {
                segmentData.add(
                    SegmentData(
                        kmSegmentCounter,
                        segment,
                        (kmShiftAndOffset[kmSegmentCounter].shift - kmShiftAndOffset[kmSegmentCounter - 1].shift)
                    )
                )
              //  println( segmentData[kmSegmentCounter].length)
            }

            // Если столб является последним
            if (kmSegmentCounter == kmPoint.lastIndex) {
                segment = mutableListOf()
                // Начало сегмента
                segment.add(kmCrossPoints[kmSegmentCounter + 1])

                for (axisCounter in nextPoint..lastPoint) {
                    segment.add(axis[axisCounter])
                }
                // Сохраняем конечную точку сегмента(по совместительству оси)
                kmCrossPoints.add(axis[axis.lastIndex])

                segmentData.add(
                    SegmentData(
                        kmSegmentCounter + 1,
                        segment,
                        (kmShiftAndOffset[kmSegmentCounter].totalLength - kmShiftAndOffset[kmSegmentCounter].shift)
                    )
                )
            }
        }
        return  segmentData
    }
}


class SegmentData(
    val numOfSeg: Int,
    val segment: MutableList<Coordinate>,
    val length: Double
) {

}
