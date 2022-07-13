package com.example.myapplication3.location

import com.example.myapplication3.location.calc.*
import com.example.myapplication3.location.data.roadDacha
import org.junit.Test
import kotlin.Pair


internal class MyCalcTest {


    private val testDistanceMarks: Map<Int, com.example.myapplication3.location.kmpluscalc.data.Coordinate> =
        roadDacha.distanceMarks
    private val testPath: Array<com.example.myapplication3.location.kmpluscalc.data.Coordinate> =
        roadDacha.axis

    // Заранее проверенные положения опорных точек
    private val knownDistanceMarks: Map<Int, Pair<Double, Double>> = mapOf(
        1 to Pair(804.11, 5.97),
        2 to Pair(1068.49, -3.30),
        3 to Pair(1132.85, 4.20)
    )

    // Тестовые точки на дороге
    data class TestPoint(
        val name: String,
        val coordinate: Coordinate,
        val kmPlusOffset: KmPlusOffset
    )

    private val points: List<TestPoint> = listOf(
        TestPoint(
            name = "Начало парковки (0+360, R 9.5)",
            coordinate = Coordinate(84.9333383060032, 56.4488557703840),
            kmPlusOffset = KmPlusOffset(0, 360.0, 9.5)
        ),
        TestPoint(
            name = "Не доезжая ЛЭП (0+785, R 5.7)",
            coordinate = Coordinate(84.939519790763, 56.4471799709039),
            kmPlusOffset = KmPlusOffset(0, 785.0, 5.7)
        ),
        TestPoint(
            name = "Чуть позже ЛЭП (1+021 R 4.3)",
            coordinate = Coordinate(84.9401600146328, 56.4471080939880),
            kmPlusOffset = KmPlusOffset(1, 021.3, 4.3)
        ),
        TestPoint(
            name = "Съезд в поле направо после ЛЭП (1+524 R 7.9)",
            coordinate = Coordinate(84.9480027570299, 56.4459193406610),
            kmPlusOffset = KmPlusOffset(1, 524.5, 7.9)
        ),
        TestPoint(
            name = "Съезд после ЛЭП в поле налево (1+812 L 8.3)",
            coordinate = Coordinate(84.9521063373097, 56.4447347087405),
            kmPlusOffset = KmPlusOffset(1, 812.4, -8.3)
        ),
        TestPoint(
            name = "Напротив дерева (1+1068; 2+000 R 4.4)",
            coordinate = Coordinate(84.9546837617013, 56.4429497990569),
            kmPlusOffset = KmPlusOffset(2, 0.0, 4.4)
        ),
        TestPoint(
            name = "Съезд налево после дерева 1 (2+330 L 13.1)",
            coordinate = Coordinate(84.9565129930468, 56.4401790136846),
            kmPlusOffset = KmPlusOffset(2, 330.7, -13.1)
        ),
        TestPoint(
            name = "Съезд после дерева налево 2 (2+963 L 12.2)",
            coordinate = Coordinate(84.9579139623627, 56.4345738705636),
            kmPlusOffset = KmPlusOffset(2, 962.5, -12.2)
        ),
        TestPoint(
            name = "Дача (3+448 R 5.5)",
            coordinate = Coordinate(84.9586023413717, 56.4291355996175),
            kmPlusOffset = KmPlusOffset(3, 448.6, 5.5)
        ), TestPoint(
            name = "Где-то перед осью(вблизи)",
            coordinate = Coordinate(84.92844151490924, 56.45211223488928),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Где-то перед осью (Офис ИндорСофт)",
            coordinate = Coordinate(84.96663646493998, 56.4934304759938),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "На оси(вдали, дом)",
            coordinate = Coordinate(84.9907912102568, 56.448854580263024),
            kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)
        ),
        TestPoint(
            name = "Точка - слепой угол для ЛЭП",
            coordinate = Coordinate(84.93974286429041, 56.44672957292902),
            kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)
        ),
        TestPoint(
            name = "Где-то за осью(вблизи)",
            coordinate = Coordinate(
                84.92844151490924,
                56.45211223488928
            ),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Где-то за осью (Офис ИндорСофт)",
            coordinate = Coordinate(84.96663646493998, 56.4934304759938),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "На оси(вдали, дом)",
            coordinate = Coordinate(84.9907912102568, 56.448854580263024),
            kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между ЛЭП и Деревом(право)",
            coordinate = Coordinate(84.94865446477132, 56.443727863973955),
            kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между ЛЭП и Деревом(лево)",
            coordinate = Coordinate(84.9509718933602, 56.44661587544344),
            kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Началом и ЛЭП(право)",
            coordinate = Coordinate(84.92946733325827, 56.447664365831535),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Началом и ЛЭП(лево)",
            coordinate = Coordinate(84.93587244838587, 56.45013022340554),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Деревом и Деревней(право)",
            coordinate = Coordinate(84.95202137633278, 56.43757165634534),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Деревом и Деревней(лево)",
            coordinate = Coordinate(84.96063663168862, 56.43852856444963),
            kmPlusOffset = KmPlusOffset(0, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Деревом и Деревней(право далеко)",
            coordinate = Coordinate(84.94083309593512, 56.43318748916113),
            kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)
        ),
        TestPoint(
            name = "Между Деревом и Деревней(слева далеко)",
            coordinate = Coordinate(85.01464149431683, 56.43663147761087),
            kmPlusOffset = KmPlusOffset(2, 0.0, 0.0)
        ),
        TestPoint(
            name = "За последней осевой точкой(близко)",
            coordinate = Coordinate(84.95672504862763, 56.42038502646851),
            kmPlusOffset = KmPlusOffset(3, 0.0, 0.0)
        ),
        TestPoint(
            name = "За последней осевой точкой(далеко)",
            coordinate = Coordinate(84.96668140849089, 56.37249895485142),
            kmPlusOffset = KmPlusOffset(3, 0.0, 0.0)
        ),
        TestPoint(
            name = "Точка - слепой угол для ЛЭП",
            coordinate = Coordinate(84.93974286429041, 56.44672957292902),
            kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)
        )

    )

    @Test
    fun testToadKmSegment(point: Int): List<Double> {

        val r1 = KilometerPointsCalc()
        r1.kmSegments(axis, distanceMarks)
        println(
            "MYCALC ${points[point].name} "
        )
        val res = KmPlusMeterCalc().checkKmPluM(
            r1.kmCrossPoints,
            points[point].coordinate,
            r1.segmentData
        )
        res.shift = (res.shift * 1000)
        res.offset = (res.offset * 1000)

        println("km = ${res.km}")
        println("m = ${res.shift}")
        println("off = ${res.offset}\n")
        return listOf(res.km.toDouble(), res.shift.toInt()/1000.toDouble(), res.offset.toInt()/1000.toDouble())

    }
    // Dacha.test.testKmPlusService
}