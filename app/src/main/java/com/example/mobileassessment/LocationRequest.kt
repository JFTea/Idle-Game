package com.example.mobileassessment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class LocationRequest : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_request)
        val locationPermBut = findViewById<Button>(R.id.locationPermBut)

        // Requests permission to the location service of the device
        locationPermBut.setOnClickListener{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Swaps to the loading activity to load the application
        val intent = Intent(this, LoadingActivity::class.java)
        startActivity(intent)
    }
}