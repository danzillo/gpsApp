package com.example.myapplication3.location

import android.app.Application
import org.junit.Assert
import org.junit.Test
import net.sf.geographiclib.*

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
                    axis[counter].getLong(),
                    axis[counter + 1].getLong(),
                    axis[counter].getLat(),
                    axis[counter + 1].getLat()
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
                    axis[counter].getLat(),
                    axis[counter].getLong(),
                    axis[counter + 1].getLat(),
                    axis[counter + 1].getLong()
                ).s12
            counter += 1
        }
        Assert.assertEquals(3625.184, lengthOfRoad, 0.01)
    }
}