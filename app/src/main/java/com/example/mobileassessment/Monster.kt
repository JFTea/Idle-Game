package com.example.mobileassessment

import java.io.Serializable
import kotlin.random.Random

class Monster(var name: String, var imageURL: String, var level: String, var errorCode: Int) : Serializable
{
    // The max health value of the monster
    var maxHealth: Int = 0

    // The monsters current health value
    var currentHealth: Int = 0
    init {
        calculateHealth()
    }

    // Calculates the range of health from the monsters level
    // The monsters level is gotten from the Digimon API
    private fun calculateHealth()
    {
        if(level == "In Training" || level == "Rookie"){
            maxHealth = Random.nextInt(50,100)
            currentHealth = maxHealth
        }
        else if(level == "Fresh" || level == "Champion"){
            maxHealth = Random.nextInt(150,200)
            currentHealth = maxHealth
        }
        else{
            maxHealth = Random.nextInt(250,300)
            currentHealth = maxHealth
        }
    }
}