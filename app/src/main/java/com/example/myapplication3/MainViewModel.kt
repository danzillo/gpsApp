package com.example.myapplication3

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"


    var lastLocation: Location? = null

    private var _isDecimalPosition = false
    val isDecimalPosition: Boolean
        get() = _isDecimalPosition

    // Позволяет менять отображение GPS координат
    fun decimalOrNot() {
        _isDecimalPosition = !_isDecimalPosition
    }

    init {
        Log.i(TAG, "MainViewModelCreated")
    }


}