package com.example.myapplication3

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication3.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.util.*
//TODO перенос всех наработок в ViewModel+LiveData izuchenie

// моментальное обновление данных геолокации
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val GPS_PERMISSION_CODE = 101
    private val STATE_LOCATION_FORMAT = "locationFormat"
    private val STATE_LOCATION = "location"

    private var isDecimalPosition: Boolean = false
    private var lastLocation: Location? = null

    private val locationRequest = LocationRequest()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return

            if (locationResult.locations.isNotEmpty()) {

                val location = locationResult.lastLocation
                lastLocation = location

                Log.i(
                    TAG,
                    "onLocationResult: ${location.time}, ${location.longitude}, ${location.latitude}"
                )

                // Выведем координату на экран
                updateLocationText()
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
        isDecimalPosition = savedInstanceState?.getBoolean(STATE_LOCATION_FORMAT) ?: false

        lastLocation = savedInstanceState?.getParcelable(STATE_LOCATION)

        // Обновить текст на экране
        updateLocationText()

        binding.button.setOnClickListener {
            isDecimalPosition = !isDecimalPosition
            updateLocationText()
        }
    }

    // Здесь весь текст локации
    private fun updateLocationText() {
        if (isDecimalPosition) {
            binding.button.text = "Переключить на DMS координаты"
        } else {
            binding.button.text = "Переключить на DD координаты"
        }

        lastLocation ?: return

        if (isDecimalPosition) {
            binding.longitude.text = "${lastLocation!!.longitude}°"
            binding.latitude.text = "${lastLocation!!.latitude}°"
        } else {
            binding.longitude.text = longitudeDecDegToDegMinSec(lastLocation!!.longitude)
            binding.latitude.text = latitudeDecDegToDegMinSec(lastLocation!!.latitude)
        }

        if (lastLocation!!.hasBearing()) binding.azimut.text =
            "${lastLocation!!.bearing}°" else binding.bearingAccuracy.text = "-"

        if (lastLocation!!.hasBearingAccuracy()) binding.bearingAccuracy.text =
            "${lastLocation!!.bearingAccuracyDegrees} м" else binding.bearingAccuracy.text = "-"

        if (lastLocation!!.hasAltitude()) binding.altitude.text =
            "${lastLocation!!.altitude.toInt()} м" else binding.altitude.text = "-"

        binding.currentDate.text = formatDate(lastLocation!!)
        binding.currentTime.text = formatTime(lastLocation!!)

        if (lastLocation!!.hasSpeed()) binding.currentSpeed.text =
            "${(lastLocation!!.speed * 100).toInt() / 100.0} м/c" else binding.currentSpeed.text =
            "-"

        if (lastLocation!!.hasAccuracy()) binding.accuracySpeed.text =
            "${lastLocation!!.speedAccuracyMetersPerSecond}" else binding.accuracySpeed.text = "-"

        binding.provider.text = lastLocation!!.provider


        //TODO: Обновление остальных полей из location

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
    private fun longitudeDecDegToDegMinSec(decDeg: Double) = decDegToDegMinSec(decDeg, "E", "W")

    /*
    Преобразует десятичные градусы
    в градусы, минуты, секунды долготы и добавляет название полушария
    */
    private fun latitudeDecDegToDegMinSec(decDeg: Double) = decDegToDegMinSec(decDeg, "N", "S")

    private fun decDegToDegMinSec(
        decDeg: Double,
        positiveChar: String = "",
        negChar: String = ""
    ): String {
        val sphereName: String
        val deg = decDeg.toInt()
        val min = ((decDeg - deg) * 60).toInt()
        val sec = ((decDeg - deg - (min.toDouble() / 60)) * 3600).toInt()
        sphereName = if (deg > 0) positiveChar else negChar
        return "$sphereName ${deg}° ${min}' ${sec}\""
    }

    // Запрашиваем разрешения на определение местоположения (локацию)??
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&

            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.i(TAG, "checkSelfPermission failed! Request …")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GPS_PERMISSION_CODE
            )
            return

        }

        Log.i(TAG, "checkSelfPermission succeed! requestLocationUpdates …")
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
        Log.i(TAG, "onPause")
        stopLocationUpdates()
    }

    // Обновление геолокации при взаимодействии с активити
    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        startLocationUpdates()
    }

    //  Проверка получения/не получения разрешения на использование GPS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            GPS_PERMISSION_CODE -> {

                Log.i(TAG, "onRequestPermissionsResult: GPS_PERMISSION_CODE")

                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    Toast.makeText(
                        this,
                        "Разрешение на использование GPS получено!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        "Без разрешения использования GPS невозможна работа программы!",
                        Toast.LENGTH_LONG
                    ).show()

                }
                return
            }

            else -> {
                Log.i(TAG, "onRequestPermissionsResult: some other code ($requestCode)…")

                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    //сохраняем значение, определяющее способ отображения информации
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState.apply {
            putBoolean(STATE_LOCATION_FORMAT, isDecimalPosition)
            putParcelable(STATE_LOCATION, lastLocation)
        })
    }
}
