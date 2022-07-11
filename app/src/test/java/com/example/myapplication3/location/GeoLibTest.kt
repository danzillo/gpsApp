package com.example.myapplication3.location

import com.example.myapplication3.location.calc.*
import net.sf.geographiclib.*
import org.junit.Assert
import org.junit.Test
import kotlin.Pair

internal class GeoLibTest {

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
        /*       TestPoint(
                   name = "Напротив дерева (1+1068; 2+000 R 4.4)",
                   coordinate = Coordinate(84.9546837617013, 56.4429497990569),
                   kmPlusOffset = KmPlusOffset(2, 0.0, 4.4)
               ),*/
        TestPoint(
            name = "Напротив дерева (1+1068; 2+000 R 4.4)",
            coordinate = Coordinate(84.9546837617013, 56.4429497990569),
            kmPlusOffset = KmPlusOffset(1, 1068.49, 4.4)
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
        )
    )


    @Test
    fun testToadKmSegment() {
        fun testOnePoint(testPoint: TestPoint) {
            val r1 = KilometerPointsCalc()
            r1.kmSegments(axis, distanceMarks)
            println("-=[ Test: ${testPoint.name} ]=------------------------------------------------")
            val res = KmPlusMeterCalc().checkKmPluM(
                r1.kmCrossPoints,
                testPoint.coordinate,
                r1.segmentData
            )
            println("km = ${res.km}")
            println("m = ${res.shift}")
            println("off = ${res.offset}\n")
            Assert.assertEquals(testPoint.kmPlusOffset.km, res.km)
            Assert.assertEquals(testPoint.kmPlusOffset.meter, res.shift, 0.2)
            Assert.assertEquals(testPoint.kmPlusOffset.offset, res.offset, 0.2)
        }

        for (point in points)
            testOnePoint(point)
    }

    @Test
    fun testBlindAngle() {
        val testList = mutableListOf(
            Coordinate(84.881719, 56.468855),
            Coordinate(84.881150, 56.469313),
            Coordinate(84.882063, 56.469612)
        )
        val pos = Coordinate( 84.880215, 56.469253)
        val pos2 = Coordinate( 84.880864, 56.469328)
        println(ShiftAndOffsetCalc().shiftAndOffsetCalc(testList, pos2).crossPoint)
        Assert.assertEquals(56.469313, ShiftAndOffsetCalc().shiftAndOffsetCalc(testList, pos).crossPoint.latitude, 0.02)
    }

    @Test
    fun testAzimuthSign() {
        val g1 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            54.0, 85.1
        )
        println("g1 → ${g1.azi1}")
        Assert.assertEquals(90.0, g1.azi1, 0.1)

        val g2 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            54.0, 84.9
        )
        println("g2 ← ${g2.azi1}")
        Assert.assertEquals(-90.0, g2.azi1, 0.1)

        val g3 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            53.9, 84.9
        )
        println("g3 ↙ ${g3.azi1}")
        Assert.assertEquals(-149.4, g3.azi1, 0.1)

        val g4 = Geodesic.WGS84.Inverse(
            54.0, 85.0,
            53.9, 85.1
        )
        println("g4 ↘ ${g4.azi1}")
        Assert.assertEquals(149.4, g4.azi1, 0.1)
    }
}