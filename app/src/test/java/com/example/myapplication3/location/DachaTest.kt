package com.example.myapplication3.location

import com.example.myapplication3.location.data.roadDacha
import com.example.myapplication3.location.kmpluscalc.KmPlusCalculatorService
import com.example.myapplication3.location.kmpluscalc.data.Coordinate
import com.example.myapplication3.location.kmpluscalc.data.KmPlus
import com.example.myapplication3.location.kmpluscalc.data.KmPlusOffset
import com.example.myapplication3.location.kmpluscalc.kmpluscalculator.RoadSegment
import net.sf.geographiclib.Geodesic
import net.sf.geographiclib.GeodesicData
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.Pair
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

class DachaTest {

    /**
     * Координаты точек по тестовой дороге.
     * Парами: долгота, широта.
     */
    private val testPath: Array<Coordinate> = roadDacha.axis

    /**
     * Координаты опорных точек (вместо км-столбов).
     */
    private val testDistanceMarks: Map<Int, Coordinate> = roadDacha.distanceMarks

    // Заранее проверенная длина тестовой дороги, метры.
    private val knownPathLen = 3625.184

    // Требуемая точность расчёта длины
    private val toleranceForLen = 0.001

    // Заранее проверенные положения опорных точек
    private val knownDistanceMarks: Map<Int, Pair<Double, Double>> = mapOf(
        1 to Pair(804.11, 5.97),
        2 to Pair(1068.49, -3.30),
        3 to Pair(1132.85, 4.20)
    )

    // Требуемые точности сравнения (смещения, отступа)
    private val toleranceShift = 0.015

    // Тестовые точки на дороге
    data class TestPoint(
        val name: String,
        val coordinate: Coordinate,
        val kmPlusOffset: KmPlusOffset
    )

    private val points: List<TestPoint> = listOf(         TestPoint(             name = "Начало парковки (0+360, R 9.5)",             coordinate = Coordinate(84.9333383060032, 56.4488557703840),             kmPlusOffset = KmPlusOffset(0, 360.0, 9.5)         ),         TestPoint(             name = "Не доезжая ЛЭП (0+785, R 5.7)",             coordinate = Coordinate(84.939519790763, 56.4471799709039),             kmPlusOffset = KmPlusOffset(0, 785.0, 5.7)         ),         TestPoint(             name = "Чуть позже ЛЭП (1+021 R 4.3)",             coordinate = Coordinate(84.9401600146328, 56.4471080939880),             kmPlusOffset = KmPlusOffset(1, 021.3, 4.3)         ),         TestPoint(             name = "Съезд в поле направо после ЛЭП (1+524 R 7.9)",             coordinate = Coordinate(84.9480027570299, 56.4459193406610),             kmPlusOffset = KmPlusOffset(1, 524.5, 7.9)         ),         TestPoint(             name = "Съезд после ЛЭП в поле налево (1+812 L 8.3)",             coordinate = Coordinate(84.9521063373097, 56.4447347087405),             kmPlusOffset = KmPlusOffset(1, 812.4, -8.3)         ),         TestPoint(             name = "Напротив дерева (1+1068; 2+000 R 4.4)",             coordinate = Coordinate(84.9546837617013, 56.4429497990569),             kmPlusOffset = KmPlusOffset(2, 0.0, 4.4)         ),         TestPoint(             name = "Съезд налево после дерева 1 (2+330 L 13.1)",             coordinate = Coordinate(84.9565129930468, 56.4401790136846),             kmPlusOffset = KmPlusOffset(2, 330.7, -13.1)         ),         TestPoint(             name = "Съезд после дерева налево 2 (2+963 L 12.2)",             coordinate = Coordinate(84.9579139623627, 56.4345738705636),             kmPlusOffset = KmPlusOffset(2, 962.5, -12.2)         ),         TestPoint(             name = "Дача (3+448 R 5.5)",             coordinate = Coordinate(84.9586023413717, 56.4291355996175),             kmPlusOffset = KmPlusOffset(3, 448.6, 5.5)         ), TestPoint(             name = "Где-то перед осью(вблизи)",             coordinate = Coordinate(84.92844151490924, 56.45211223488928),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Где-то перед осью (Офис ИндорСофт)",             coordinate = Coordinate(84.96663646493998, 56.4934304759938),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "На оси(вдали, дом)",             coordinate = Coordinate(84.9907912102568, 56.448854580263024),             kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)         ),         TestPoint(             name = "Точка - слепой угол для ЛЭП",             coordinate = Coordinate(84.93974286429041, 56.44672957292902),             kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)         ),         TestPoint(             name = "Где-то за осью(вблизи)",             coordinate = Coordinate(                 84.92844151490924,                 56.45211223488928             ),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Где-то за осью (Офис ИндорСофт)",             coordinate = Coordinate(84.96663646493998, 56.4934304759938),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "На оси(вдали, дом)",             coordinate = Coordinate(84.9907912102568, 56.448854580263024),             kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)         ),         TestPoint(             name = "Между ЛЭП и Деревом(право)",             coordinate = Coordinate(84.94865446477132, 56.443727863973955),             kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)         ),         TestPoint(             name = "Между ЛЭП и Деревом(лево)",             coordinate = Coordinate(84.9509718933602, 56.44661587544344),             kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)         ),         TestPoint(             name = "Между Началом и ЛЭП(право)",             coordinate = Coordinate(84.92946733325827, 56.447664365831535),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Между Началом и ЛЭП(лево)",             coordinate = Coordinate(84.93587244838587, 56.45013022340554),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Между Деревом и Деревней(право)",             coordinate = Coordinate(84.95202137633278, 56.43757165634534),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Между Деревом и Деревней(лево)",             coordinate = Coordinate(84.96063663168862, 56.43852856444963),             kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)         ),         TestPoint(             name = "Между Деревом и Деревней(право далеко)",             coordinate = Coordinate(84.94083309593512, 56.43318748916113),             kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)         ),         TestPoint(             name = "Между Деревом и Деревней(слева далеко)",             coordinate = Coordinate(85.01464149431683, 56.43663147761087),             kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)         ),         TestPoint(             name = "За последней осевой точкой(близко)",             coordinate = Coordinate(84.95672504862763, 56.42038502646851),             kmPlusOffset = KmPlusOffset(3, 0.0, 0.0)         ),         TestPoint(             name = "За последней осевой точкой(далеко)",             coordinate = Coordinate(84.96668140849089, 56.37249895485142),             kmPlusOffset = KmPlusOffset(3, 0.0, 0.0)         ),         TestPoint(             name = "Точка - слепой угол для ЛЭП",             coordinate = Coordinate(84.93974286429041, 56.44672957292902),             kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)         )      )


    private val tolerancePointPos = 0.2

    @Test
    fun test_PathDistance() {
        var lenAccumulator = 0.0

        val calcMillis = measureTimeMillis {
            for (i in 0 until testPath.size - 1) {
                val inv: GeodesicData = Geodesic.WGS84.Inverse(
                    testPath[i].y, testPath[i].x,
                    testPath[i + 1].y, testPath[i + 1].x
                )
                lenAccumulator += inv.s12
            }
        }

        assertEquals(knownPathLen, lenAccumulator, toleranceForLen)

        println("Total length of path is $lenAccumulator meters")
        println("Calculation time $calcMillis millis")
    }

    @Test
    fun test_RoadSegment() {
        val rs = RoadSegment.createFromLineString(testPath)

        // Проверим расчёт общей длины
        assertEquals(knownPathLen, rs.length, toleranceForLen)

        // Проверим расчёт по известным точкам (координата) -> (смещение и отступ)
        val p1 = rs.calcShiftAndOffsetFromLocation(testDistanceMarks[1]!!)
        println(p1)
        println("difference = ${knownDistanceMarks[1]!!.first - p1.meter!!}")
        assertEquals(knownDistanceMarks[1]!!.first, p1.meter!!, toleranceShift)
        assertEquals(knownDistanceMarks[1]!!.second.absoluteValue, p1.offsetAbs, toleranceShift)

        val p2 = rs.calcShiftAndOffsetFromLocation(testDistanceMarks[2]!!)
        println(p2)
        println("difference = ${knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first - p2.meter!!}")
        assertEquals(
            knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first,
            p2.meter!!, toleranceShift
        )
        assertEquals(knownDistanceMarks[2]!!.second.absoluteValue, p2.offsetAbs, toleranceShift)

        val p3 = rs.calcShiftAndOffsetFromLocation(testDistanceMarks[3]!!)
        println(p3)
        println("difference = ${knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first + knownDistanceMarks[3]!!.first - p3.meter!!}")
        assertEquals(
            knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first + knownDistanceMarks[3]!!.first,
            p3.meter!!, toleranceShift
        )
        assertEquals(knownDistanceMarks[3]!!.second.absoluteValue, p3.offsetAbs, toleranceShift)

        // Проверим обратный расчёт (смещение и отступ) -> (координата)
        val s1 = rs.calcLocationFromShiftAndOffset(
            knownDistanceMarks[1]!!.first,
            knownDistanceMarks[1]!!.second
        )!!
        val d1: GeodesicData =
            Geodesic.WGS84.Inverse(s1.y, s1.x, testDistanceMarks[1]!!.y, testDistanceMarks[1]!!.x)
        println(s1)
        println("Difference is ${d1.s12} meter")
        assertEquals(0.0, d1.s12, toleranceShift)

        // Проверим обратный расчёт (смещение и отступ) -> (координата)
        val s2 = rs.calcLocationFromShiftAndOffset(
            knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first,
            knownDistanceMarks[2]!!.second
        )!!
        val d2: GeodesicData =
            Geodesic.WGS84.Inverse(s2.y, s2.x, testDistanceMarks[2]!!.y, testDistanceMarks[2]!!.x)
        println(s2)
        println("Difference is ${d2.s12} meter")
        assertEquals(0.0, d2.s12, toleranceShift)

        // Проверим обратный расчёт (смещение и отступ) -> (координата)
        val s3 = rs.calcLocationFromShiftAndOffset(
            knownDistanceMarks[1]!!.first + knownDistanceMarks[2]!!.first + knownDistanceMarks[3]!!.first,
            knownDistanceMarks[3]!!.second
        )!!
        val d3: GeodesicData =
            Geodesic.WGS84.Inverse(s3.y, s3.x, testDistanceMarks[3]!!.y, testDistanceMarks[3]!!.x)
        println(s3)
        println("Difference is ${d3.s12} meter")
        assertEquals(0.0, d3.s12, toleranceShift)
    }

    @Test
    fun test_KmPlusCalcService() {
        val service = KmPlusCalculatorService()
        val pathId = service.addRoadPathCalc(
            startKmDouble = 0.0,
            startKmPlus = KmPlus(0, 0.0),
            axis = testPath,
            kmPoints = testDistanceMarks
        )

        val my = MyCalcTest()

        fun checkPoint(p: TestPoint): List<Double>? {
            println("\nDACHACALC ${p.name}")
            val kmP = service.calcKmPlusFromLocation(pathId, p.coordinate)!!
            println("km = ${kmP.km}")
            println("m = ${kmP.meter}")
            println("off = ${kmP.offset}\n")
            kmP.meter = (kmP.meter * 1000)
            kmP.offset = (kmP.offset * 1000)
            return kmP.km?.let {
                listOf(
                    it.toDouble(),
                    kmP.meter.toInt() / 1000.toDouble(),
                    kmP.offset.toInt() / 1000.toDouble()
                )
            }
        }

        for (p in 0 until points.size - 1) {
            val r1 = checkPoint(points[p])
            val r2 = my.testToadKmSegment(p)
            println(r1)
            println(listOf(r2))
         //   assertEquals(r1, r2)
        }


    }
}
