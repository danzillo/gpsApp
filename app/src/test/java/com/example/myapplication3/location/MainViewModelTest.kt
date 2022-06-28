package com.example.myapplication3.location

import android.app.Application
import org.junit.Assert
import org.junit.Test
import net.sf.geographiclib.*
import org.junit.Assert.assertEquals
import kotlin.math.cos

internal class MainViewModelTest {

    private val viewModelTest = MainViewModel(application = Application())

    // Тестирование метода определения координат для точек антиподов
    @Test
    fun lengthBetweenCoordinate() {
        var lengthOfRoad: Double = 0.0
        val lastCoordinateIndex = axis.lastIndex
        var counter: Int = 0
        while (counter < lastCoordinateIndex) {
            lengthOfRoad +=
                viewModelTest.lengthBetweenCoordinate(
                    axis[counter].longitude,
                    axis[counter + 1].longitude,
                    axis[counter].latitude,
                    axis[counter + 1].latitude
                )
            counter += 1
        }
        Assert.assertEquals(3625.184, lengthOfRoad, 0.01)
    }

    // Тестирование с помощью библиотеки
    @Test
    fun geoLibLength() {
        var lengthOfRoad: Double = 0.0
        val lastCoordinateIndex = axis.lastIndex
        var counter = 0
        while (counter < lastCoordinateIndex) {
            lengthOfRoad +=
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12
            counter += 1
        }
        assertEquals(3625.184, lengthOfRoad, 0.01)
    }


    @Test
    fun geoLibCrossPoint() {
        var lengthToStolb: Double = Double.MAX_VALUE
        var lengthOfRoad: Double = 0.0
        var minLengthToStolb: Double = Double.MAX_VALUE
        val lastCoordinateIndex = axis.lastIndex
        var counter = 0
        var counter2 = 0
        var saveCounter = 0

        // Проходимся по всем вершинам
        while (counter < lastCoordinateIndex) {

            // Находим расстояние от вершины до столба
            lengthToStolb =
                Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    distanceMarks[0].latitude,
                    distanceMarks[0].longitude
                ).s12

            // Если найденное расстояние меньше того, что было, то сохраняем его
            // и длину дороги между точками
            if (lengthToStolb < minLengthToStolb) {
                minLengthToStolb = lengthToStolb
                lengthOfRoad = Geodesic.WGS84.Inverse(
                    axis[counter].latitude,
                    axis[counter].longitude,
                    axis[counter + 1].latitude,
                    axis[counter + 1].longitude
                ).s12
                // Сохраняем счетчик, чтобы потом найти общую длину дороги до столба
                saveCounter = counter
            }

            counter += 1
        }

        /* Зная длину до столба, длину участка дороги между точками
           можно найти cos, косинус * | длДоСтолба | = длине проекции длДоСтолба
           на длину дороги*/
        val projection = minLengthToStolb * cos(lengthOfRoad / minLengthToStolb)
        lengthOfRoad = 0.0

        while (counter2 < saveCounter) {
            lengthOfRoad +=
                Geodesic.WGS84.Inverse(
                    axis[counter2].latitude,
                    axis[counter2].longitude,
                    axis[counter2 + 1].latitude,
                    axis[counter2 + 1].longitude
                ).s12
            counter2 += 1
        }

        println("lengthOfRoad = $lengthOfRoad")
        println("projection = $projection")
        println("(projection + lengthOfRoad) = ${projection + lengthOfRoad}")

        assertEquals(90, projection + lengthOfRoad)
    }

}