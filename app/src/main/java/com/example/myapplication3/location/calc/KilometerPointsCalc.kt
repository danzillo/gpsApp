package com.example.myapplication3.location.calc

/**
 * Класс для
 * - разбиения оси дороги на километровые участки.
 * - хранения километровых участков дороги.
 */
class KilometerPointsCalc {
    private val prevPoint = 0
    private var nextPoint = 0
    private val lastPoint = axis.lastIndex
    private val kmShiftAndOffset = mutableListOf<ShiftAndOffset>()

    /**
     * TODO: Описание
     * TODO: listOf()
     */
    val segmentData = mutableListOf<SegmentData>()

    /**
     * TODO: Write docs
     */
    val kmCrossPoints = mutableListOf<Coordinate>()

    /**
     * TODO: Назначить его конструктором
     *
     *  Разбитие оси на километровые сегменты
     */
    fun kmSegments(
        axis: MutableList<Coordinate>,
        kmPoint: MutableList<Coordinate>
    ): MutableList<SegmentData> {
        for (kmSegmentCounter in 0..kmPoint.lastIndex) {
            var segment = mutableListOf<Coordinate>()

            // kmShiftAndOffset для хранения информации класса ShiftAndOffset
            kmShiftAndOffset.add(
                ShiftAndOffsetCalc().shiftAndOffsetCalc(
                    axis,
                    kmPoint[kmSegmentCounter]
                )
            )

            // Запись сегмента для 1 столба
            if (kmSegmentCounter == 0) {

                // Начальная точка оси
                kmCrossPoints.add(axis[prevPoint])

                // Записываем все точки между началом и 1 столбом
                for (axisCounter in prevPoint..kmShiftAndOffset[kmSegmentCounter].prevPoint) {
                    segment.add(axis[axisCounter])
                }

                // Координаты проекции столба
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
            segment.add(kmCrossPoints[kmSegmentCounter + 1])

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
            } else {
                segmentData.add(
                    SegmentData(
                        kmSegmentCounter,
                        segment,
                        kmShiftAndOffset[kmSegmentCounter].shift
                    )
                )
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
                        (kmShiftAndOffset[kmSegmentCounter].totalLength)
                    )
                )
            }
        }
        return segmentData
    }

}
