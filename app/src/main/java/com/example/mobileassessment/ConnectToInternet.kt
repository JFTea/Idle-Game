package com.example.mobileassessment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ConnectToInternet : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_to_internet)
        val tryAgainButton = findViewById<Button>(R.id.reconnectBut)

        // Changes back tot he loading activity if the user tries to connect to the internet again
        tryAgainButton.setOnClickListener{
            val intent = Intent(this, LoadingActivity::class.java)
            startActivity(intent)
        }
    }
}