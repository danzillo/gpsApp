package com.example.myapplication3.location

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication3.databinding.ActivityMainBinding

// моментальное обновление данных геолокации
class MainActivity : AppCompatActivity() {

    // Биндинг для получения доступа к элементам слоя activity_main.xml
    lateinit var binding: ActivityMainBinding

    // Создаем экземпляр ViewModel
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Подключение ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Подключимся к получению координат
        viewModel.startLocationUpdates(this)
        viewModel.lastLocation.observe(this) {
            updateLocationOnScreen()
        }

        // Назначим обработчик нажатия на кнопку
        binding.button.setOnClickListener {
            viewModel.switchGpsFormat()
            updateLocationOnScreen()
        }

        // Начальное отображение данных
        updateLocationOnScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
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
            binding.provider.text = location.provider.toString()
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
            binding.provider.text = "-"
        }
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val GPS_PERMISSION_CODE = 101
    }
}
