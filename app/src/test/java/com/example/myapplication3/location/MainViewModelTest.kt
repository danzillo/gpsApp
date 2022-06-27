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

        fun lengthBetweenCoordinate(
            longitude: Double,
            longitude2: Double,
            latitude: Double,
            latitude2: Double
        ): Double {

            // Преобразование градусов в радианы
            val radLongitude = longitude * 3.14159265358979 / 180
            val radLongitude2 = longitude2 * 3.14159265358979 / 180

            val radLatitude = latitude * 3.14159265358979 / 180
            val radLatitude2 = latitude2 * 3.14159265358979 / 180

            // Расчет cos & sin для формулы
            val cosLatitude = cos(radLatitude)
            val cosLatitude2 = cos(radLatitude2)

            val sinLatitude = sin(radLatitude)
            val sinLatitude2 = sin(radLatitude2)

            val longitudeSubtraction = radLongitude2 - radLongitude
            val cosLongitudeSubtraction = cos(longitudeSubtraction)
            val sinLongitudeSubtraction = sin(longitudeSubtraction)

            // Формула для получения расстояния между двумя точками (работает для антиподов)
            return abs(
                atan(
                    ((cosLatitude2 * sinLongitudeSubtraction).pow(2) + ((cosLatitude * sinLatitude2) - (sinLatitude * cosLatitude2 * cosLongitudeSubtraction)).pow(
                        2
                    )).pow(0.5) /
                            (sinLatitude * sinLatitude2 + cosLatitude * cosLatitude2 * cosLongitudeSubtraction)
                ) * 6_378_137
            )
            // TODO: посмотреть подробнее про радиус для WGS-84
        }
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