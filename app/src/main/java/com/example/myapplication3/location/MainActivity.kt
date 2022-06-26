package com.example.myapplication3.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication3.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest

// моментальное обновление данных геолокации
class MainActivity : AppCompatActivity() {

    // Биндинг для получения доступа к элементам слоя activity_main.xml
    lateinit var binding: ActivityMainBinding

    // Создаем экземпляр ViewModel
    lateinit var viewModel: MainViewModel

    // Создаем экземпляр для сохранения режима отображения
    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Location Manager
    var imHere : Location? = null
    private var locationManager : LocationManager? = null
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            imHere = location
            binding.provider.text = location.provider
            //Log.i(TAG, "LocationLongitude: ${ location.provider}")
        }
        fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        fun onProviderEnabled(provider: String) {}
        fun onProviderDisabled(provider: String) {}
    }

    private val locationRequest = LocationRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Подключение ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Загружаем данные при запуске программы
        pref = getPreferences(MODE_PRIVATE)
        editor = pref.edit()

       // loadCoordinateTypeData()
        if(loadCoordinateTypeData() && viewModel.isDecimalPosition.value != true)
            viewModel.switchGpsFormat()

        // Подключимся к получению координат
        viewModel.startLocationUpdates(this)
        viewModel.lastLocation.observe(this) {
            updateLocationOnScreen()
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        imHere = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(imHere == null){
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)

        }

        // Назначим обработчик нажатия на кнопку
        binding.button.setOnClickListener {
            viewModel.switchGpsFormat()
            updateLocationOnScreen()
            //locationManager?.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0L, 0f, locationListener)
         }

        // Начальное отображение данных
        updateLocationOnScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
            saveCoordinateTypeData(viewModel.isDecimalPosition.value)
        Log.i(TAG, "Destroy app, SharedPtrf:${loadCoordinateTypeData()} Button ${viewModel.isDecimalPosition.value}")
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

    @SuppressLint("SetTextI18n")
    fun updateLocationOnScreen() {

        val location = viewModel.lastLocation.value

        //viewModel.isDecimalPosition.value = loadCoordinateTypeData()

        if (location != null) {
            if (viewModel.isDecimalPosition.value == true) {
                binding.button.text = "Переключить на DMS координаты"
                binding.latitude.text = location.latitude.toString() + "°"
                binding.longitude.text = location.longitude.toString() + "°"
            } else {
                binding.button.text = "Переключить на DEC координаты"
                binding.latitude.text = viewModel.latitudeDecDegToDegMinSec(location.latitude)
                binding.longitude.text = viewModel.longitudeDecDegToDegMinSec(location.longitude)
            }
            binding.azimut.text = location.bearing.toString() + "°"
            binding.bearingAccuracy.text = location.bearingAccuracyDegrees.toString() + " м"
            binding.altitude.text = location.altitude.toInt().toString() + " м"
            binding.currentDate.text = viewModel.formatDate(location, "dd.MM.YYYY")
            binding.currentTime.text = viewModel.formatDate(location, "HH:mm:ss")
            binding.currentSpeed.text =
                ((location.speed * 100).toInt() / 100).toString() + " м/c"
            binding.accuracySpeed.text = location.speedAccuracyMetersPerSecond.toString() + " м"
           // binding.provider.text = location.provider.toString()
        } else {
            if (viewModel.isDecimalPosition.value == true) {
                binding.button.text = "Переключить на DMS координаты"
                binding.latitude.text = "-"
                binding.longitude.text = "-"
            } else {
                binding.button.text = "Переключить на DEC координаты"
                binding.latitude.text = "-"
                binding.longitude.text = "-"
            }
            binding.azimut.text = "-"
            binding.bearingAccuracy.text = "-"
            binding.altitude.text = "-"
            binding.currentDate.text = "-"
            binding.currentTime.text = "-"
            binding.currentSpeed.text = "-"
            binding.accuracySpeed.text = "-"
           // binding.provider.text = "-"
        }
    }

    // Сохраняем и загружаем информацию в преференс
    private fun saveCoordinateTypeData(displayOfCoordiante: Boolean?) {
        if (displayOfCoordiante != null) {
            editor.putBoolean(COORDINATE_DISPLAY_PREFERENCE_KEY, displayOfCoordiante)
        }
        editor.apply()
    }

    //  Выгружаем данные из преференса для выбора режима отображения координат на дисплее
    private fun loadCoordinateTypeData(): Boolean {
        return pref.getBoolean(COORDINATE_DISPLAY_PREFERENCE_KEY, false)
    }


    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val COORDINATE_DISPLAY_PREFERENCE_KEY = "location"
        private const val GPS_PERMISSION_CODE = 101
    }
}

private fun LocationManager?.requestLocationUpdates(gpsProvider: String, l: Long, fl: Float, locationListener: LocationListener) {

}
