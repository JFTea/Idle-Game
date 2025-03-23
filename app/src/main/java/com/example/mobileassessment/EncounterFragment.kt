package com.example.mobileassessment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
class EncounterFragment : Fragment()
{

    // The reference to the game manager for this fragment
    private val gameManager: GameManager by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encounter, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val completeText = view.findViewById<TextView>(R.id.completeText)
        val monsterView = view.findViewById<ImageView>(R.id.imageView)
        val healthBar = view.findViewById<ProgressBar>(R.id.healthBar)

        // Starts the dungeon if there is a dungeon
        if(gameManager.currentDungeon != null)
        {
            gameManager.startDungeon(healthBar, monsterView)
        }
        else
        {
            completeText.visibility = View.VISIBLE
            monsterView.visibility = View.GONE
        }

        // Damages the current monster when its image view is clicked
        monsterView.setOnClickListener{

            // Decreases the current monster's health by the player active damage amount
            gameManager.currentDungeon!!.currentMonster.currentHealth -= gameManager.userProfile.activeDamage
            healthBar.progress = gameManager.currentDungeon!!.currentMonster.currentHealth

            // If the monster has 0 or less hp the next monster is gotten from the dungeon
            if(gameManager.currentDungeon!!.currentMonster.currentHealth <= 0)
            {
                // Gets the next encounter, if it returns true the dungeon is complete and the dungeon complete screen is shown
                if(gameManager.nextEncounter(healthBar, monsterView))
                {
                    monsterView.visibility = View.GONE
                    completeText.visibility = View.VISIBLE
                }
            }
        }
    }
}