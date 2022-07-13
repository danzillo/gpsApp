package com.example.myapplication3.location

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication3.databinding.ActivityGpsDataBinding
import com.example.myapplication3.databinding.ActivityKmPlusBinding
import com.example.myapplication3.location.calc.Coordinate

class KmPlusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKmPlusBinding

    // Создаем экземпляр ViewModel
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKmPlusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Подключение ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Загружаем данные о формате координат при запуске программы
        viewModel.pref = getPreferences(MODE_PRIVATE)
        viewModel.editor = viewModel.pref.edit()

        if (viewModel.loadCoordinateTypeData() && viewModel.isDecimalPosition.value != true)
            viewModel.switchGpsFormat()

        // Подключимся к получению координат
        viewModel.startLocationUpdates(this)
        viewModel.lastLocation.observe(this) {
            updateLocationOnScreen()
        }

        binding.switchKmPluToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Начальное отображение данных
        updateLocationOnScreen()

    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
        viewModel.saveCoordinateTypeData(viewModel.isDecimalPosition.value)
        Log.i(
            TAG,
            "Pause activity"
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopLocationUpdates()
        viewModel.saveCoordinateTypeData(viewModel.isDecimalPosition.value)
        Log.i(
            TAG,
            "Stop activity"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
        viewModel.saveCoordinateTypeData(viewModel.isDecimalPosition.value)
        Log.i(
            TAG,
            "Destroy app, SharedPref:${viewModel.loadCoordinateTypeData()} Button ${viewModel.isDecimalPosition.value}"
        )
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

            binding.azimuthData.text = location.bearing.toString() + "°"
            binding.accuracyData.text = location.accuracy.toString() + " м"
            binding.timeData.text = viewModel.formatDate(location, "HH:mm:ss")
            binding.speedData.text =
                ((location.speed * 100).toInt() / 100).toString() + " м/c"
            binding.satelliteData.text =
                "20 спутников"
            val r1 = viewModel.showCurrentPos(Coordinate(location.longitude, location.latitude))
            binding.kmPlusData.text =
                "${r1[0]} + ${r1[1].toInt()}"
            if (r1[2] > 0) {
                binding.offset.text =
                    "Right: ${r1[2].toInt()}"
            } else {
                binding.offset.text =
                    "Left: ${r1[2]}"
            }

        } else {

            binding.azimuthData.text = "-°"
            binding.accuracyData.text = "- м"
            binding.timeData.text = "-"
            binding.speedData.text = "- м/c"
            binding.satelliteData.text =
                "-"
            binding.kmPlusData.text = "-"
            binding.offset.text = "-"
        }
    }

    companion object {
        private val TAG = GpsDataActivity::class.simpleName
        private const val GPS_PERMISSION_CODE = 101
    }
}