package com.example.mobileassessment

import android.os.Build
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import coil.load
import java.util.Date
import java.util.Timer

@RequiresApi(Build.VERSION_CODES.M)
class GameManager: ViewModel()
{
    // The current active dungeon
    var currentDungeon: Dungeon? = null

    // A bool that is true when the current dungeon is complete
    var dungeonComplete = false

    /*
        Timer task was inspired by the code found in an example written in Java
        Author: Nobatgeldi
        Accessed: 20/01/2024
        URL: https://gist.github.com/Nobatgeldi/db64b6d470b3a1343d30e382f6ef2151
    */
    // The timer than runs overtime damage every second
    private lateinit var timer: Timer
    // End of inspired code

    // The users profile that will be stored when the app closes
    var userProfile = UserProfile()

    // This is run every second to deal idle damage to the current monster in the active dungeon
    fun damagePerSecond(healthBar: ProgressBar, monsterView: ImageView)
    {

        // Deals the idle damage to the monster and updates the monsters health bar
        currentDungeon!!.currentMonster.currentHealth -= userProfile.tickDamage
        healthBar.progress = currentDungeon!!.currentMonster.currentHealth

        // Checks if the monster has been defeated and gets the next encounter if it has
        if(currentDungeon!!.currentMonster.currentHealth <= 0)
        {
            nextEncounter(healthBar, monsterView)
        }
    }

    // Starts a new active dungeon
    fun startDungeon(healthBar: ProgressBar, monsterView: ImageView)
    {
        // Instantiates a new timer for the new dungeon
        timer = Timer()

        /*
           This line is based upon the documentation of the Coil library
           Author: Coil Contributors
           Accessed: 13/01/2024
           URL: https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose
        */
        // This line loads the image from a url using the Coil library
        monsterView.load(currentDungeon!!.currentMonster.imageURL)
        // End of cited code

        // Sets the health bar UI to the maximum of the first monster in the dungeon
        healthBar.max = currentDungeon!!.currentMonster.maxHealth
        healthBar.progress = currentDungeon!!.currentMonster.currentHealth

        // Sets the current dungeon field in the user profile
        userProfile.currentDungeon = currentDungeon

        /*
            Timer task was inspired by the code found in an example written in Java
            Author: Nobatgeldi
            Accessed: 20/01/2024
            URL: https://gist.github.com/Nobatgeldi/db64b6d470b3a1343d30e382f6ef2151
        */
        // Schedules the idle damage to happen every second
        timer.schedule(OnTick(this, healthBar, monsterView),Date(System.currentTimeMillis()), 1000)
        // End of inspired code
    }

    // Gets the next encounter in the dungeon, returns true if the dungeon is complete
    fun nextEncounter(healthBar: ProgressBar, monsterView: ImageView): Boolean
    {
        // Gets the new encounter from the dungeon
        val encounter = currentDungeon!!.getEncounter(userProfile)

        // Checks if the encounter is null and loads the next monster if it is not null
        if(encounter != null)
        {
            /*
               This line is based upon the documentation of the Coil library
               Author: Coil Contributors
               Accessed: 13/01/2024
               URL: https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose
            */
            // This line loads the image from a url using the Coil library
            monsterView.load(encounter.imageURL)
            // End of cited code
            healthBar.max = encounter.maxHealth
            healthBar.progress = currentDungeon!!.currentMonster.currentHealth
            dungeonComplete = false
        }
        else // If the encounter is null then the dungeon is complete
        {
            // Sets the flags for the dungeon being complete
            dungeonComplete = true
            currentDungeon = null

            // Cancels the idle damage timer
            timer.cancel()
        }
        return dungeonComplete
    }
}