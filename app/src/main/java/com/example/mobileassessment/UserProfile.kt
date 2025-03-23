package com.example.mobileassessment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.SurfaceView
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.util.Date

class UserProfile : Serializable
{

    // The amount of idle damage done by the user
    var tickDamage = 1

    // The amount of active damage done by the user
    var activeDamage = 2

    // The array of possible upgrades the user can unlock
    var upgrades = Array(2){0}

    // The currency collected by the user
    var currency1 = 0

    // The current active dungeon
    var currentDungeon: Dungeon? = null

    // The damage done from time when the application was closed
    var damageFromTime = 0

    // The time when the application last closed
    // This is initialised with when the user profile is created as a temporary value
    var lastLogin = Date(System.currentTimeMillis())

    init
    {
        calcDamageNumbers()
    }

    // Calculates the damage numbers for the player based on their upgrades
    private fun calcDamageNumbers()
    {
        activeDamage = 2 * upgrades[0] + 2
        tickDamage = 1 * upgrades[1] + 1
    }

    // Adds an upgrade to the player
    @SuppressLint("SetTextI18n")
    fun addUpgrade(surfaceView: SurfaceView, id:Int, upgradeButton: ImageButton, expectedCost: TextView)
    {
        // Calculates the cost of the upgrade
        val cost = (upgrades[id] + 1) * 5

        // Checks if the player can afford the upgrade
        if(currency1 >= (upgrades[id] + 1) * 5){
            // Sets the tracker for the new upgrade
            surfaceView.setBackgroundColor(Color.parseColor("#FFC107"))
            upgrades[id] += 1
            currency1 -= cost

            // Updates the cost text for the next upgrade
            expectedCost.text = "Cost: " + ((upgrades[id] + 1) * 5).toString()

            if(upgrades[id] >= 5){
                upgradeButton.isClickable = false
            }
            calcDamageNumbers()
        }
    }

    // Calculates the damage done from time when the application was closed
    @RequiresApi(Build.VERSION_CODES.M)
    fun calculateDungeonDamage()
    {
        if(currentDungeon != null)
        {
            while(damageFromTime > 0)
            {
                // Checks if there are still monsters in the dungeon
                if(currentDungeon!!.monsters.size > 0)
                {
                    // Checks if the damage left in damageFromTime is enough to defeat the current monster
                    if(currentDungeon!!.currentMonster.currentHealth < damageFromTime || currentDungeon!!.currentMonster.currentHealth < 1)
                    {
                        // Reduces the remaining damage to be dealt by the current monsters remaining health
                        damageFromTime -= currentDungeon!!.currentMonster.currentHealth
                        // Checks to see if the dungeon is complete
                        if(currentDungeon!!.getEncounter(this) == null)
                        {
                            currentDungeon = null
                            break
                        }
                    }
                    else
                    {
                        // Sets the next monster and reduces its health by any remaining damage from time
                        currentDungeon!!.currentMonster.currentHealth -= damageFromTime
                        break
                    }
                }
            }
            // Ends the dungeon if it has no monsters
            if(currentDungeon!!.monsters.size < 1)
            {
                currentDungeon = null
            }
        }

    }
}