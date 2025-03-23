package com.example.mobileassessment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DungeonFragment : Fragment(), Observer<Dungeon>
{

    // The game manager reference for this fragment
    private val gameManager: GameManager by activityViewModels()

    // The layout that holds the UI for when the dungeon is being made
    private lateinit var loadingLayout: ConstraintLayout

    /*
        This line is inspired by the examples from the Android Developer documentation
        Author: Android Developer (individual author name unknown)
        Accessed: 20/01/2024
        URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
    */
    // The executor service that allows for the dungeon to be created on new thread
    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)
    // End of cited line

    // The layout that is displayed if there is an existing active dungeon
    private lateinit var completeDungeonLayout: ConstraintLayout

    // The layout that is shown when selecting a new dungeon
    private lateinit var selectDungeonLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dungeon, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        completeDungeonLayout = view.findViewById(R.id.completeDungeonLayout)
        selectDungeonLayout = view.findViewById(R.id.dungeonSelectLayout)

        val easyRewardsText = view.findViewById<TextView>(R.id.easyExpected)
        val easyRadio = view.findViewById<RadioGroup>(R.id.easyRadioGroup)
        val easySelect = view.findViewById<Button>(R.id.easySelect)
        val easyShort = view.findViewById<RadioButton>(R.id.easyShort)
        val easyMed = view.findViewById<RadioButton>(R.id.easyMed)
        val easyLong = view.findViewById<RadioButton>(R.id.easyLong)

        val normalRewardText = view.findViewById<TextView>(R.id.normExpected)
        val normalRadio = view.findViewById<RadioGroup>(R.id.normalRadioGroup)
        val normalSelect = view.findViewById<Button>(R.id.normSelect)
        val normalShort = view.findViewById<RadioButton>(R.id.normShort)
        val normalMed = view.findViewById<RadioButton>(R.id.normMed)
        val normalLong = view.findViewById<RadioButton>(R.id.normLong)

        val hardRewardText = view.findViewById<TextView>(R.id.hardExpected)
        val hardRadio = view.findViewById<RadioGroup>(R.id.hardRadioGroup)
        val hardSelect = view.findViewById<Button>(R.id.hardSelect)
        val hardShort = view.findViewById<RadioButton>(R.id.hardShort)
        val hardMed = view.findViewById<RadioButton>(R.id.hardMed)
        val hardLong = view.findViewById<RadioButton>(R.id.hardLong)

        loadingLayout = view.findViewById(R.id.loadingLayout)
        loadingLayout.visibility = View.GONE

        easySelect.setOnClickListener {
            val radioID = easyRadio.checkedRadioButtonId


            var dungeonLength = ""
            when (radioID)
            {
                R.id.easyShort ->
                {
                    dungeonLength = "short"
                }

                R.id.easyMed ->
                {
                    dungeonLength = "medium"
                }

                R.id.easyLong ->
                {
                    dungeonLength = "long"
                }
            }

            /*
                This code was inspired by the examples given in the documentation for LiveData
                Author: Android Developers (Author name unknown)
                Accessed: 20/01/2024
                URL: https://developer.android.com/topic/libraries/architecture/livedata
            */
            // Generates the dungeon asynchronously to free up the main thread for onDraw()
            val easyData: LiveData<Dungeon> = liveData {
                loadingLayout.visibility = View.VISIBLE
                val temp = Dungeon("easy", dungeonLength)

                /*
                    The use of the executor service here is inspired by the examples from the Android Developer documentation
                    Author: Android Developer (individual author name unknown)
                    Accessed: 20/01/2024
                    URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
                */
                // Generates the dungeon on a separate thread
                executorService.execute {
                    temp.generateMonsters()
                }
                // End of inspired code
                emit(temp)
            }

            // Runs when the monster has been generated and the data emitted
            val easyObserver = Observer<Dungeon>
            {

                gameManager.currentDungeon = it
                loadingLayout.visibility = View.INVISIBLE
                gameManager.dungeonComplete = false
                completeDungeonLayout.visibility = View.VISIBLE
                selectDungeonLayout.visibility = View.INVISIBLE

            }

            // Sets up the observer to view the live data
            easyData.observe(viewLifecycleOwner, easyObserver)

            // End cited code
        }

        // Updates the easy expected rewards text based on the length selected for the dungeon
        easyShort.setOnClickListener{
            easyRewardsText.text = "Expected Rewards: 3-5 gems"
        }

        easyMed.setOnClickListener{
            easyRewardsText.text = "Expected Rewards: 10-15 gems"
        }

        easyLong.setOnClickListener{
            easyRewardsText.text = "Expected Rewards: 20-30 gems"
        }

        normalSelect.setOnClickListener {
            val radioID = normalRadio.checkedRadioButtonId

            var dungeonLength = ""
            when (radioID)
            {
                R.id.normShort ->
                {
                    dungeonLength = "short"
                }

                R.id.normMed ->
                {
                    dungeonLength = "normal"
                }

                R.id.normLong ->
                {
                    dungeonLength = "long"
                }
            }

            /*
                This code was inspired by the examples given in the documentation for LiveData
                Author: Android Developers (Author name unknown)
                Accessed: 20/01/2024
                URL: https://developer.android.com/topic/libraries/architecture/livedata
             */
            // Generates the dungeon asynchronously to free up the main thread for onDraw()
            val normalData: LiveData<Dungeon> = liveData {
                loadingLayout.visibility = View.VISIBLE
                val temp = Dungeon("normal", dungeonLength)

                /*
                    The use of the executor service here is inspired by the examples from the Android Developer documentation
                    Author: Android Developer (individual author name unknown)
                    Accessed: 20/01/2024
                    URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
                */
                // Generates the dungeon on a separate thread
                executorService.execute {
                    temp.generateMonsters()
                }
                emit(temp)
            }

            // Runs when the monster has been generated and the data emitted
            val normalObserver = Observer<Dungeon>
            {

                gameManager.currentDungeon = it
                loadingLayout.visibility = View.INVISIBLE
                gameManager.dungeonComplete = false
                completeDungeonLayout.visibility = View.VISIBLE
                selectDungeonLayout.visibility = View.INVISIBLE
            }

            // Sets up the observer to view the live data
            normalData.observe(viewLifecycleOwner, normalObserver)

            // End cited code
        }

        // Updates the normal expected rewards text based on the length selected for the dungeon
        normalShort.setOnClickListener{
            normalRewardText.text = "Expected Rewards: 6-10 gems"
        }

        normalMed.setOnClickListener{
            normalRewardText.text = "Expected Rewards: 20-30 gems"
        }

        normalLong.setOnClickListener{
            normalRewardText.text = "Expected Rewards: 40-60 gems"
        }

        hardSelect.setOnClickListener {
            val radioID = hardRadio.checkedRadioButtonId

            var dungeonLength = ""
            when (radioID)
            {
                R.id.hardShort ->
                {
                    dungeonLength = "short"
                }

                R.id.hardMed ->
                {
                    dungeonLength = "medium"
                }

                R.id.hardLong ->
                {
                    dungeonLength = "long"
                }
            }
            /*
                This code was inspired by the examples given in the documentation for LiveData
                Author: Android Developers (Author name unknown)
                Accessed: 20/01/2024
                URL: https://developer.android.com/topic/libraries/architecture/livedata
            */
            // Generates the dungeon asynchronously to free up the main thread for onDraw()
            val hardData: LiveData<Dungeon> = liveData {
                loadingLayout.visibility = View.VISIBLE
                val temp = Dungeon("hard", dungeonLength)

                /*
                    The use of the executor service here is inspired by the examples from the Android Developer documentation
                    Author: Android Developer (individual author name unknown)
                    Accessed: 20/01/2024
                    URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
                */
                // Generates the dungeon on a separate thread
                executorService.execute {
                    temp.generateMonsters()
                }
                emit(temp)
            }

            // Runs when the monster has been generated and the data emitted
            val hardObserver = Observer<Dungeon>
            {

                gameManager.currentDungeon = it
                gameManager.dungeonComplete = false
                loadingLayout.visibility = View.INVISIBLE
                completeDungeonLayout.visibility = View.VISIBLE
                selectDungeonLayout.visibility = View.INVISIBLE
            }

            // Sets up the observer to view the live data
            hardData.observe(viewLifecycleOwner, hardObserver)

            // End cited code
        }

        // Updates the hard expected rewards text based on the length selected for the dungeon
        hardShort.setOnClickListener{
            hardRewardText.text = "Expected Rewards: 9-15 gems"
        }

        hardMed.setOnClickListener{
            hardRewardText.text = "Expected Rewards: 30-45 gems"
        }

        hardLong.setOnClickListener{
            hardRewardText.text = "Expected Rewards: 60-90 gems"
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume()
    {
        super.onResume()
        // Checks if there is an active dungeon and displays the correct layout accordingly
        if(gameManager.dungeonComplete)
        {
            completeDungeonLayout.visibility = View.INVISIBLE
            selectDungeonLayout.visibility = View.VISIBLE
        }
        else
        {
            completeDungeonLayout.visibility = View.VISIBLE
            selectDungeonLayout.visibility = View.INVISIBLE
        }
    }

    override fun onChanged(value: Dungeon)
    {
        loadingLayout.visibility = View.VISIBLE
    }
}