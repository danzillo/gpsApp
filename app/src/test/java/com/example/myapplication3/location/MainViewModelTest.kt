package com.example.myapplication3.location

import android.app.Application
import org.junit.Assert
import org.junit.Test


internal class MainViewModelTest {

    private val viewModelTest = MainViewModel(application = Application())

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
        Assert.assertEquals(4, lengthOfRoad)
    }

}