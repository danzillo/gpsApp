package com.example.myapplication3.location

import android.annotation.SuppressLint
import android.content.Intent
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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Открытие карты при нажатии на кнопку
        binding.switchMainToMap.setOnClickListener{
           val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // Открытие GPS информации при нажатии на кнопку
        binding.switchMainToGPS.setOnClickListener{
            val intent = Intent(this, GpsDataActivity::class.java)
            startActivity(intent)
        }

        // Открытие КМ+ при нажатии на кнопку
        binding.switchMainToKmPlus.setOnClickListener{
            val intent = Intent(this, KmPlusActivity::class.java)
            startActivity(intent)
        }
    }


    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}
