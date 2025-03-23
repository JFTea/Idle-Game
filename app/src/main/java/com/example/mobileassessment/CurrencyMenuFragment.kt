package com.example.mobileassessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.util.Date
import java.util.Timer

class CurrencyMenuFragment : Fragment()
{
    // A reference to the game manager instance for the app
    private val gameManager: GameManager by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_currency_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // Gets the currency text UI element
        val currency1Text = view.findViewById<TextView>(R.id.currency1Text)

        /*
            Timer task was inspired by the code found in an example written in Java
            Author: Nobatgeldi
            Accessed: 20/01/2024
            URL: https://gist.github.com/Nobatgeldi/db64b6d470b3a1343d30e382f6ef2151
         */
        // Updates the currency display text every second to make sure it shows an accurate value
        val timer = Timer()
        timer.schedule(CurrencyManager(gameManager,currency1Text), Date(System.currentTimeMillis()), 1000)
        // End of inspired code
    }
}