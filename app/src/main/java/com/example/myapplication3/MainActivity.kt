package com.example.myapplication3

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapplication3.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.util.*

/*
TODO:
Переключатель двух измерений
onRequestPermissionsResult доработка
*/

class MainActivity : AppCompatActivity() {

    private var WRITE_PERMISSION = 101

    private val locationRequest = LocationRequest()
    private var buttonIdentity: Boolean = false
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            if (locationResult.locations.isNotEmpty()) {
                val location = locationResult.lastLocation
                if(buttonIdentity){
                    binding.longitude.text = "${location.longitude}°"
                    binding.latitude.text = "${location.latitude}°"
                }
                else{
                    binding.longitude.text = "${location.latitude}°"
                    binding.latitude.text = "${location.longitude}°"
                }
//                binding.longitude.text = "${location.longitude}°"
//                binding.latitude.text = "${location.latitude}°"
                binding.azimut.text = "${location.bearing}°"
                binding.bearingAccuracy.text = "${location.bearingAccuracyDegrees} м"
                //поработать над высотой
                binding.altitude.text = "${location.altitude}м"
                binding.currentDate.text = formatDate(location)
                binding.currentTime.text = formatTime(location)
                binding.currentSpeed.text = "${location.speed} м/c"
                binding.accuracySpeed.text = "${location.speedAccuracyMetersPerSecond}"
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


        //Биндим кнопку для переключения режимов
        binding.button.setOnClickListener {
            buttonIdentity = !buttonIdentity
            Log.e(toString.toString(buttonIdentity))
        }
    }

    //Преобразуем системное время из location в дату
    private fun formatDate(location: Location): String {
        val formatDate: DateFormat = SimpleDateFormat("dd.MM.yyyy")
        return formatDate.format(Date(location.time))
    }

    // Преобразуем системное время из location во время
    private fun formatTime(location: Location): String {
        val formatTime: DateFormat = SimpleDateFormat("HH:mm:ss")
        return formatTime.format(Date(location.time))
    }

    // Запрашиваем разрешения на определение местоположения (локацию)
    private fun startLocationUpdates() {
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), WRITE_PERMISSION
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // Заканичваем обновлениее позициноирования
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

    // Доработать эту часть
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

