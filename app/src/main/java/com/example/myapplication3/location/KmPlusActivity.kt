package com.example.myapplication3.location

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication3.databinding.ActivityKmPlusBinding

class KmPlusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKmPlusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKmPlusBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}