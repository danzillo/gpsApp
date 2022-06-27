package com.example.myapplication3.location

import android.app.Application
import org.junit.Assert
import org.junit.Test
import kotlin.math.*


internal class MainViewModelTest {

    private val mapper = MainViewModel(application = Application())
    @Test
    fun lengthBetweenCoordinate(){
        val longI= 5
        mapper.lengthBetweenCoordinate
    }

    object LengthBetweenCoordinate {


    }

    @Test
    fun test() {
        var lengthOfRoad: Double = 0.0
        val lastCoordinateIndex = axis.lastIndex
        var counter: Int = 0

        while (counter < lastCoordinateIndex) {
            lengthOfRoad += LengthBetweenCoordinate.lengthBetweenCoordinate(
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