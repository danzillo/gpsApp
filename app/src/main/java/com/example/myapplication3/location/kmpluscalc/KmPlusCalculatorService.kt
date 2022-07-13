package com.example.myapplication3.location.kmpluscalc

import com.example.myapplication3.location.kmpluscalc.data.Coordinate
import com.example.myapplication3.location.kmpluscalc.data.KmPlus
import com.example.myapplication3.location.kmpluscalc.data.KmPlusOffset
import com.example.myapplication3.location.kmpluscalc.kmpluscalculator.RoadKmPlusAxis
import java.util.*

class KmPlusCalculatorService {

    /**
     * Набор данных в памяти для повторных расчётов.
     * В условиях нехватки ресурсов, допускается очистка этого хранилища.
     */
    private val inMemoryAxes = mutableMapOf<UUID, RoadKmPlusAxis>()

    /**
     * Вычислить КМ+ из координаты для Объекта контроля.
     * Необходимо соблюдение следующих условий:
     * - Объект контроля должен иметь проектную ось
     * - Объект контроля должен содержать КМ-столбы
     * @param axisId - идентификатор Объекта контроля
     * @param coordinate - координата точки, для которой требуется найти эксплуатационный пикетаж
     * @return - эксплуатационное положение от километровой отметки (КМ+)
     */
    fun calcKmPlusFromLocation(
        axisId: UUID,
        coordinate: Coordinate
    ): KmPlusOffset? {
        // Получить калькулятор
        val calc = inMemoryAxes[axisId] ?: return null

        // Произвести поиск КМ+
        val calcRes: KmPlusOffset? = calc.calcKmPlusFromLocation(coordinate)

        // Конвертировать результат
        return calcRes ?: KmPlusOffset(meter = 0.0)
    }

    /**
     * Вычислить координату по километровому столбу и метровому смещению от него для Объекта контроля.
     * Необходимо соблюдение следующих условий:
     * - Объект контроля должен иметь проектную ось
     * - Объект контроля должен содержать КМ-столбы
     * @param axisId - идентификатор Объекта контроля
     * @param kmPlusOffset - эксплуатационное положение от километровой отметки (КМ+)
     * @return - координата искомой точки
     */
    fun calcLocationFromKmPlus(
        axisId: UUID,
        kmPlusOffset: KmPlusOffset
    ): Coordinate? {
        // Получить калькулятор
        val calc = inMemoryAxes[axisId] ?: return null

        // Произвести поиск координаты
        return calc.calcLocationFromKmPlus(kmPlusOffset)
    }

    /**
     * Вычислить примерный расход памяти в байтах.
     */
    fun memorySize(): Long {
        val axesSizes: List<Long> = inMemoryAxes.toList().map { it.second.memorySize() }
        return axesSizes.sum()
    }

    /**
     * Создать новый калькулятор километража.
     */
    fun addRoadPathCalc(
        id: UUID? = null,
        startKmDouble: Double,
        startKmPlus: KmPlus,
        axis: Array<Coordinate>,
        kmPoints: Map<Int, Coordinate>
    ): UUID {
        // Создать калькулятор для этого запроса
        val calc = RoadKmPlusAxis.create(
            startKmDouble,
            startKmPlus,
            axis,
            kmPoints
        )

        val newUUID = id ?: UUID.randomUUID()

        // Сохранить его в списке загруженных
        inMemoryAxes[newUUID] = calc

        // Получить калькулятор из памяти
        return newUUID
    }

}