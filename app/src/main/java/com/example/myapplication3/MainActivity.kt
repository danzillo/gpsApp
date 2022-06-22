package com.example.myapplication3

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication3.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var GPS_PERMISSION_CODE = 101

    private val locationRequest = LocationRequest()
    private var buttonState: Boolean = false
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return

            if (locationResult.locations.isNotEmpty()) {
                val location = locationResult.lastLocation

                // Вариации отображения данных долготы/широты
                if (buttonState) {
                    binding.longitude.text = "${location.longitude}°"
                    binding.latitude.text = "${location.latitude}°"
                } else {
                    binding.longitude.text = longitudeDecDegToDegMinSec(location.longitude)
                    binding.latitude.text = latitudeDecDegToDegMinSec(location.latitude)
                }

                if (location.hasBearing()) binding.azimut.text =
                    "${location.bearing}°" else binding.bearingAccuracy.text = "-"

                if (location.hasBearingAccuracy()) binding.bearingAccuracy.text =
                    "${location.bearingAccuracyDegrees}м" else binding.bearingAccuracy.text = "-"

                if (location.hasAltitude()) binding.altitude.text =
                    "${location.altitude.toInt()}м" else binding.altitude.text = "-"

                binding.currentDate.text = formatDate(location)
                binding.currentTime.text = formatTime(location)

                if (location.hasSpeed()) binding.currentSpeed.text =
                    "${(location.speed * 100).toInt() / 100.0}м/c" else binding.currentSpeed.text =
                    "-"

                if (location.hasAccuracy()) binding.accuracySpeed.text =
                    "${location.speedAccuracyMetersPerSecond}" else binding.accuracySpeed.text = "-"

                binding.provider.text = location.provider
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Формируем требования по точности местоположения
        locationRequest.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // Получаем провайдер местоположения от комплекса сенсоров
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Восстанавливаем значение buttonState из bundle
        buttonState = savedInstanceState?.getBoolean("buttonState") ?: false

        // Бинд кнопки для переключения режимов отображения долготы/широты
        binding.button.setOnClickListener {
            buttonState = !buttonState
        }

        // При пуске спрашиваем разрешение на использование GPS
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GPS_PERMISSION_CODE
            )
            return
        }

    }

    // Преобразуем системное время из location в дату
    private fun formatDate(location: Location): String {
        val formatDate: DateFormat = SimpleDateFormat("dd.MM.yyyy")
        return formatDate.format(Date(location.time))
    }

    // Преобразуем системное время из location во время
    private fun formatTime(location: Location): String {
        val formatTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        return formatTime.format(Date(location.time))
    }

    /*
    Преобразует десятичные градусы
    в градусы, минуты, секунды долготы и добавляет название полушария
    */
    private fun longitudeDecDegToDegMinSec(decDeg: Double): String {
        val sphereName: String
        val deg = decDeg.toInt()
        val min = ((decDeg - deg) * 60).toInt()
        val sec = ((decDeg - deg - (min.toDouble() / 60)) * 3600).toInt()
        sphereName = if (deg > 180) "E" else "W"
        return "$sphereName ${deg}° ${min}' ${sec}\""
    }

    /*
    Преобразует десятичные градусы
    в градусы, минуты, секунды долготы и добавляет название полушария
    */
    private fun latitudeDecDegToDegMinSec(decDeg: Double): String {
        val sphereName: String
        val deg = decDeg.toInt()
        val min = ((decDeg - deg) * 60).toInt()
        val sec = ((decDeg - deg - (min.toDouble() / 60)) * 3600).toInt()
        sphereName = if (deg > 0) "N" else "S"
        return "$sphereName ${deg}° ${min}' ${sec}\""
    }

    // Запрашиваем разрешения на определение местоположения (локацию)??
    private fun startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GPS_PERMISSION_CODE
//            )
//            return
//        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // Заканчиваем обновление позиционирования
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Останавливаем обновление геолокации
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // Обновление геолокации при взаимодействии с активити
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    //  Проверка получения/не получения разрешения на использование GPS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GPS_PERMISSION_CODE
            -> {

                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Toast.makeText(
                        this,
                        "Разрешение на использование GPS получено!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {

                    Toast.makeText(
                        this,
                        "Без разрешения использования GPS невозможна работа программы!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    //сохраняем значение, определяющее способ отображения информации
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState.apply {
            putBoolean("buttonState", buttonState)
        })
    }
}
