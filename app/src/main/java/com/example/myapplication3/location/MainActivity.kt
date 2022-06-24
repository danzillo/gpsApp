package com.example.myapplication3.location

import android.content.pm.PackageManager
import android.nfc.Tag
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

        viewModel.startLocationUpdates(this)

        viewModel.lastLocation.observe(this) { location ->
            if (location != null) {
                binding.button.setOnClickListener {
                    viewModel.decimalOrNot()
                    viewModel.isDecimalPosition.observe(this) { decimalPosition ->
                        if (decimalPosition) {
                            binding.button.text = "Переключить на DMS координаты"
                            binding.latitude.text = location.latitude.toString() + "°"
                            binding.longitude.text = location.longitude.toString() + "°"
                        } else {
                            binding.button.text = "Переключить на DD координаты"
                            binding.latitude.text =
                                viewModel.latitudeDecDegToDegMinSec(location.latitude)
                            binding.longitude.text =
                                viewModel.latitudeDecDegToDegMinSec(location.longitude)
                        }
                    }
                }

                if (viewModel.isDecimalPosition.value == true) {
                    binding.latitude.text = location.latitude.toString() + "°"
                    binding.longitude.text = location.longitude.toString() + "°"
                } else {
                    binding.latitude.text = viewModel.latitudeDecDegToDegMinSec(location.latitude)
                    binding.longitude.text = viewModel.latitudeDecDegToDegMinSec(location.longitude)
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
            }
        }

        //начальное отображение кнопок
        if (viewModel.isDecimalPosition.value == true) {
            binding.button.text = "Переключить на DMS координаты"
        } else {
            binding.button.text = "Переключить на DD координаты"
        }
    }

    // Останавливаем обновление геолокации
    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
    }

    // Обновление геолокации при взаимодействии с активити
    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
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

    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val GPS_PERMISSION_CODE = 101
    }
}
