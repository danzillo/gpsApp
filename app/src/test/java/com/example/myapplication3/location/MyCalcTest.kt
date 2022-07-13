package com.example.myapplication3.location

import android.app.Application
import com.example.myapplication3.location.calc.*
import com.example.myapplication3.location.data.roadDacha
import com.example.myapplication3.location.kmpluscalc.KmPlusCalculatorService
import com.example.myapplication3.location.kmpluscalc.data.KmPlus
import net.sf.geographiclib.*
import org.junit.Assert
import org.junit.Test
import kotlin.Pair
import kotlin.system.measureTimeMillis

internal class MyCalcTest {


    private val testDistanceMarks: Map<Int, com.example.myapplication3.location.kmpluscalc.data.Coordinate> = roadDacha.distanceMarks
    private val testPath: Array<com.example.myapplication3.location.kmpluscalc.data.Coordinate> = roadDacha.axis

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
        ),
        TestPoint(
            name = "Где-то перед осью(вблизи)",
            coordinate = Coordinate(84.92844151490924, 56.45211223488928),
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
            name = "Точка - слепой угол для ЛЭП",
            coordinate = Coordinate(84.92844151490924, 56.45211223488928),
            kmPlusOffset = KmPlusOffset(1, 0.0, 0.0)
        )
    )

   /* @Test
    fun testToadKmSegment() {
        val service = KmPlusCalculatorService()
        val pathId = service.addRoadPathCalc(
            startKmDouble = 0.0,
            startKmPlus = KmPlus(0, 0.0),
            axis = testPath,
            kmPoints = testDistanceMarks
        )

        fun testDachaCalc(p: TestPoint){
            println("\nTesting for point ${p.name}")
            val kmP = service.calcKmPlusFromLocation(pathId, p.coordinate)!!
            println(kmP)
            Assert.assertEquals(p.kmPlusOffset.km, kmP.km)
            // assertEquals(p.kmPlusOffset.meter, kmP.meter, tolerancePointPos)
            //assertEquals(p.kmPlusOffset.offset, kmP.offset, tolerancePointPos)
            println("Coordinate -> KmPlus accepted")

            val cP = service.calcLocationFromKmPlus(pathId, p.kmPlusOffset)!!
            val gd: GeodesicData = Geodesic.WGS84.Inverse(
                p.coordinate.y, p.coordinate.x,
                cP.y, cP.x
            )
            println("$cP: distance to target ${gd.s12} meter")
        }
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
           // Assert.assertEquals(testPoint.kmPlusOffset.km, res.km)
            //Assert.assertEquals(testPoint.kmPlusOffset.meter, res.shift, 0.2)
            //Assert.assertEquals(testPoint.kmPlusOffset.offset, res.offset, 0.2)
        }

        for (point in points)
            testOnePoint(point)
    }*/
   // Dacha.test.testKmPlusService
}