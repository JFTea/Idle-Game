package com.example.mobileassessment

import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.TimerTask

/*
    Timer task was inspired by the code found in an example written in Java
    Author: Nobatgeldi
    Accessed: 20/01/2024
    URL: https://gist.github.com/Nobatgeldi/db64b6d470b3a1343d30e382f6ef2151
 */
class CurrencyManager(private val gameManager: GameManager, private val currency1TextView: TextView): TimerTask()
{
    @RequiresApi(Build.VERSION_CODES.M)
    override fun run()
    {
        // Updates the currency text at the top of the screen
        currency1TextView.text = gameManager.userProfile.currency1.toString()
    }
}