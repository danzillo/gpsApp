package com.example.myapplication3.location

import android.app.Application
import com.example.myapplication3.location.calc.axis
import org.junit.Assert
import org.junit.Test

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
}