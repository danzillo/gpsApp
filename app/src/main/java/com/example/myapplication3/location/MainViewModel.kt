package com.example.myapplication3.location

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.text.BoringLayout
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _lastLocation = MutableLiveData<Location?>(null)
    val lastLocation: LiveData<Location?> = _lastLocation

    val latitude: LiveData<String> = lastLocation.map { location ->
        return@map if (location != null) String.format("%1.4f", location.latitude) else "-"
    }

    private val _isDecimalPosition = MutableLiveData(false)
    val isDecimalPosition: LiveData<Boolean> = _isDecimalPosition

    private val context = getApplication<Application>().applicationContext

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequest = LocationRequest()
    // Позволяет менять отображение GPS координат
    fun decimalOrNot() {
        _isDecimalPosition.value = !_isDecimalPosition.value!!
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null && locationResult.locations.isNotEmpty()) {
                _lastLocation.value = locationResult.lastLocation
            }
        }
    }

    fun startLocationUpdates(activity: Activity) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&

            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Log.i(TAG, "checkSelfPermission failed! Request …")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), GPS_PERMISSION_CODE
            )
            return

        }

        // Формируем требования по точности местоположения
        locationRequest.apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        Log.i(TAG, "checkSelfPermission succeed! requestLocationUpdates …")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // Заканчиваем обновление позиционирования
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Преобразуем системное время из location в дату/время
    private fun formatDate(location: Location, pattern: String): String {
        val formatDate: DateFormat = SimpleDateFormat(pattern)
        return formatDate.format(Date(location.time))
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

    // LiveData for each button
    private fun updateLocationText() {
        if (_isDecimalPosition.value == true) {
            binding.button.text = "Переключить на DMS координаты"
        } else {
            binding.button.text = "Переключить на DD координаты"
        }

        viewModel.lastLocation ?: return

        if (viewModel.isDecimalPosition) {
            binding.longitude.text = "${viewModel.lastLocation!!.longitude}°"
            binding.latitude.text = "${viewModel.lastLocation!!.latitude}°"
        } else {
            binding.longitude.text = longitudeDecDegToDegMinSec(viewModel.lastLocation!!.longitude)
            binding.latitude.text = latitudeDecDegToDegMinSec(viewModel.lastLocation!!.latitude)
        }

        if (viewModel.lastLocation!!.hasBearing()) binding.azimut.text =
            "${viewModel.lastLocation!!.bearing}°" else binding.bearingAccuracy.text = "-"

        if (viewModel.lastLocation!!.hasBearingAccuracy()) binding.bearingAccuracy.text =
            "${viewModel.lastLocation!!.bearingAccuracyDegrees} м" else binding.bearingAccuracy.text =
            "-"

        if (viewModel.lastLocation!!.hasAltitude()) binding.altitude.text =
            "${viewModel.lastLocation!!.altitude.toInt()} м" else binding.altitude.text = "-"

        binding.currentDate.text = formatDate(viewModel.lastLocation!!)
        binding.currentTime.text = formatTime(viewModel.lastLocation!!)

        if (viewModel.lastLocation!!.hasSpeed()) binding.currentSpeed.text =
            "${(viewModel.lastLocation!!.speed * 100).toInt() / 100.0} м/c" else binding.currentSpeed.text =
            "-"

        if (viewModel.lastLocation!!.hasAccuracy()) binding.accuracySpeed.text =
            "${viewModel.lastLocation!!.speedAccuracyMetersPerSecond}" else binding.accuracySpeed.text =
            "-"

        binding.provider.text = viewModel.lastLocation!!.provider

    }

    companion object {
        private val TAG = MainViewModel::class.simpleName
        private const val GPS_PERMISSION_CODE = 101
    }
}
