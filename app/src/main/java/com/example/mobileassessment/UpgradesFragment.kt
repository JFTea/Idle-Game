package com.example.mobileassessment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class UpgradesFragment : Fragment()
{

    private val gameManager: GameManager by activityViewModels()
    private lateinit var activeButtonLayout: LinearLayout
    private lateinit var tickButtonLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upgrades, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val activeDamageUpgradeButton = view.findViewById<ImageButton>(R.id.activeDamageUpgradeBut)
        val tickDamageUpgradeButton = view.findViewById<ImageButton>(R.id.tickDamageUpgradeBut)
        val activeTracker = arrayOf(view.findViewById<SurfaceView>(R.id.activeView1),view.findViewById(R.id.activeView2),view.findViewById(R.id.activeView3),view.findViewById(R.id.activeView4),view.findViewById(R.id.activeView5))
        val tickTracker = arrayOf(view.findViewById<SurfaceView>(R.id.tickView1),view.findViewById(R.id.tickView2),view.findViewById(R.id.tickView3),view.findViewById(R.id.tickView4),view.findViewById(R.id.tickView5))
        val tickCostText = view.findViewById<TextView>(R.id.tickUpgradeCostText)
        val activeCostText = view.findViewById<TextView>(R.id.activeUpgradeCostText)

        activeButtonLayout = view.findViewById(R.id.activeButtonLayout)
        tickButtonLayout = view.findViewById(R.id.tickButtonLayout)

        // Sets the cost text for each upgrade
        activeCostText.text = "Cost: " + ((gameManager.userProfile.upgrades[0] + 1) * 5).toString()
        tickCostText.text = "Cost: " + ((gameManager.userProfile.upgrades[1] + 1) * 5).toString()

        // Sets the upgrade trackers to show what level the upgrades are already at
        for(x in 1..gameManager.userProfile.upgrades[0])
        {
            activeTracker[x-1].setBackgroundColor(Color.parseColor("#FFC107"))
        }

        for(x in 1..gameManager.userProfile.upgrades[1])
        {
            tickTracker[x-1].setBackgroundColor(Color.parseColor("#FFC107"))
        }

        // Checks to see if the user can afford any of the upgrades
        checkCosts()

        activeDamageUpgradeButton.setOnClickListener {
            // Adds the upgrade to the active damage to the user profile and updates the trackers for it
            when (gameManager.userProfile.upgrades[0])
            {
                0 ->
                {
                    gameManager.userProfile.addUpgrade(activeTracker[0], 0, activeDamageUpgradeButton, activeCostText)
                }

                1 ->
                {
                    gameManager.userProfile.addUpgrade(activeTracker[1], 0, activeDamageUpgradeButton, activeCostText)
                }

                2 ->
                {
                    gameManager.userProfile.addUpgrade(activeTracker[2], 0, activeDamageUpgradeButton, activeCostText)
                }

                3 ->
                {
                    gameManager.userProfile.addUpgrade(activeTracker[3], 0, activeDamageUpgradeButton, activeCostText)
                }

                4 ->
                {
                    gameManager.userProfile.addUpgrade(activeTracker[4], 0, activeDamageUpgradeButton, activeCostText)
                }
            }
            // Checks to see if the user can afford any of the upgrades
            checkCosts()
        }

        tickDamageUpgradeButton.setOnClickListener {
            // Adds the upgrade to the idle damage to the user profile and updates the trackers for it
            when (gameManager.userProfile.upgrades[1])
            {
                0 ->
                {
                    gameManager.userProfile.addUpgrade(tickTracker[0], 1, tickDamageUpgradeButton, tickCostText)
                }

                1 ->
                {
                    gameManager.userProfile.addUpgrade(tickTracker[1], 1, tickDamageUpgradeButton, tickCostText)
                }

                2 ->
                {
                    gameManager.userProfile.addUpgrade(tickTracker[2], 1, tickDamageUpgradeButton, tickCostText)
                }

                3 ->
                {
                    gameManager.userProfile.addUpgrade(tickTracker[3], 1, tickDamageUpgradeButton, tickCostText)
                }

                4 ->
                {
                    gameManager.userProfile.addUpgrade(tickTracker[4], 1, tickDamageUpgradeButton, tickCostText)
                }
            }
            // Checks to see if the user can afford any of the upgrades
            checkCosts()
        }
    }

    // Checks to see if the user can afford an upgrade then either shows or hides the upgrade button accordingly
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkCosts()
    {
        // Checks to see if the user can purchase an upgrade to the active damage
        if((gameManager.userProfile.upgrades[0] + 1) * 5 <= gameManager.userProfile.currency1)
        {
            activeButtonLayout.visibility = View.VISIBLE
        }
        else
        {
            activeButtonLayout.visibility = View.INVISIBLE
        }

        // Checks to see if the user can purchase an upgrade to the idle damage
        if((gameManager.userProfile.upgrades[1] + 1) * 5 <= gameManager.userProfile.currency1)
        {
            tickButtonLayout.visibility = View.VISIBLE
        }
        else
        {
            tickButtonLayout.visibility = View.INVISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        // Checks to see if the user can afford any of the upgrades
        checkCosts()
    }
}