package com.example.mobileassessment

import android.os.Build
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import java.util.TimerTask

/*
    Timer task was inspired by the code found in an example written in Java
    Author: Nobatgeldi
    Accessed: 20/01/2024
    URL: https://gist.github.com/Nobatgeldi/db64b6d470b3a1343d30e382f6ef2151
 */
class OnTick(private val gameManager: GameManager, private var healthBar: ProgressBar, private var monsterView: ImageView): TimerTask()
{
    @RequiresApi(Build.VERSION_CODES.M)
    override fun run()
    {
        // Runs the idle damage script every second
        gameManager.damagePerSecond(healthBar, monsterView)
    }
}