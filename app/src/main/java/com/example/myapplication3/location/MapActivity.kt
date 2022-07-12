package com.example.myapplication3.location

import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication3.databinding.ActivityMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    // Создаем экземпляр ViewModel
    private lateinit var viewModel: MainViewModel

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
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
        // Загружаем конфигурацию osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        //Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID; <- для установки userAgent(чтоб не получить бан на сервере osm)

        // Создаем карту
        map = binding.map
        // Устанавливаем ее тип
       // map.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Видимость кнопок zoom-a
        map.setBuiltInZoomControls(false)
        // Возможность управлять картой через touch screen
        map.setMultiTouchControls(true)
        // Начальный масштаб карты
        map.controller.setZoom(20.0)
        // Начальная точка отображения карты
        map.controller.setCenter(GeoPoint(0.0, 0.0))

        binding.switchMapToData.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateLocationOnScreen() {

        val location = viewModel.lastLocation.value
        if (location != null) {
            map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
            val prov = GpsMyLocationProvider(this)
            prov.addLocationSource(LocationManager.GPS_PROVIDER)
            val locationOverlay = MyLocationNewOverlay(prov, map)
            locationOverlay.enableMyLocation()
            locationOverlay.isDrawAccuracyEnabled
            Log.i(TAG, "${locationOverlay.isDrawAccuracyEnabled}")
            map.overlayManager.add(locationOverlay)
        }
    }

    companion object {
        private val TAG = MapActivity::class.simpleName
        private const val GPS_PERMISSION_CODE = 101
    }
}