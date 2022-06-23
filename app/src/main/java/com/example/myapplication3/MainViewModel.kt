package com.example.myapplication3

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"


//    private var _lastLocation = MutableLiveData<Location?>(null)
//    val lastLocation: LiveData<Location?>
//        get() = _lastLocation
//
//    private var _isDecimalPosition = MutableLiveData<Boolean>(false)
//    val isDecimalPosition: LiveData<Boolean>
//        get() = _isDecimalPosition

    private var _lastLocation : Location? = null
    val lastLocation: Location?
        get() = _lastLocation

    private var _isDecimalPosition : Boolean = false
    val isDecimalPosition: Boolean
        get() = _isDecimalPosition

            //Позволяет менять отображение GPS координат
    fun decimalOrNot() {
        _isDecimalPosition = !_isDecimalPosition
    }

    fun getLastLoaction(location: Location) {
        _lastLocation = location
    }

    // Wrode kak ROBIT
//    fun decimalOrNot() {
//        _isDecimalPosition.value = !_isDecimalPosition.value!!
//    }
//
//    fun getLastLocation(location: Location) {
//        _lastLocation.value = location
//    }


}
