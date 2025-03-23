package com.example.mobileassessment

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.Date
import java.util.concurrent.CountDownLatch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), LocationListener
{

    // The accurate current time from the API
    private val currentTime = Date(0,0,0,0,0)

    // The game manager reference which is shared with all the fragments
    private val gameManager: GameManager by viewModels()

    // Stores the latitude and longitude of the device
    private var locationPos = arrayOf(0.0,0.0)

    // The location manager service
    private lateinit var locationManager: LocationManager

    /*
        This line is inspired by this code which is used to wait for the response from the api to be finished
        before moving forward
        Author: foxwendy
        Accessed: 15/01/2024
        URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
    */
    // A countdown which waits for the API call
    private var waitForTime = CountDownLatch(1)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tries to get the user profile loaded from the loading activity
        try
        {
            gameManager.userProfile = intent.getSerializableExtra("UserProfile") as UserProfile
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }

        // Checks if there is an active dungeon and sets up the dungeon
        if(gameManager.userProfile.currentDungeon == null)
        {
            gameManager.dungeonComplete = true
            gameManager.currentDungeon = null
        }
        else
        {
            gameManager.dungeonComplete = false
            gameManager.currentDungeon = gameManager.userProfile.currentDungeon
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentView) as NavHostFragment
        val navController = navHostFragment.navController
        val fightButton = findViewById<ImageButton>(R.id.fightButton)
        val dungeonButton = findViewById<ImageButton>(R.id.dungeonButton)
        val upgradesButton = findViewById<ImageButton>(R.id.upgradesButton)

        // Navigates to the encounter fragment
        fightButton.setOnClickListener{
            // Sets the active image for the encounter button
            navController.navigate(R.id.encounterFragment)
            dungeonButton.setImageResource(R.drawable.caveblack)
            upgradesButton.setImageResource(R.drawable.arrowupwardblack)
            fightButton.setImageResource(R.drawable.swordwhite)
        }

        // Navigates to the dungeon selection fragment
        dungeonButton.setOnClickListener{
            // Sets the active image for the dungeon button
            navController.navigate(R.id.dungeonFragment)
            dungeonButton.setImageResource(R.drawable.cavewhite)
            upgradesButton.setImageResource(R.drawable.arrowupwardblack)
            fightButton.setImageResource(R.drawable.swordblack)
        }

        // Navigates to the upgrades fragment
        upgradesButton.setOnClickListener{
            // Sets the active image for the upgrades button
            navController.navigate(R.id.upgradesFragment)
            dungeonButton.setImageResource(R.drawable.caveblack)
            upgradesButton.setImageResource(R.drawable.uparrowwhite)
            fightButton.setImageResource(R.drawable.swordblack)
        }
    }

    // Runs when the location manager registers a change in location
    override fun onLocationChanged(location: Location)
    {
        // Saves the latitude and longitude of the device
        locationPos[0] = location.latitude
        locationPos[1] = location.longitude

        // Removes the location listener as the location is only needed once
        locationManager.removeUpdates(this)
    }

    // Saves the current user profile when the application enters the stop state
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStop()
    {
        super.onStop()
        /*
            The locationManager.isProviderEnabled code was taken from here
            Author: GAMA
            Accessed: 21/01/2024
            URL: https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
         */
        // Checks if the application has access to the location service
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            // End if cited code
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,this)
        }

        // Tries to get accurate time from the API
        try
        {
            getAccurateTime(locationPos)
        }
        catch (e: Exception) // Runs if the attempt to get accurate time from the API fails
        {
            // Stores the last login from the system time as a last resort
            gameManager.userProfile.lastLogin = Date(System.currentTimeMillis())

            /*
                This line is used to wait for the response from the api to be finished
                before moving forward
                Author: foxwendy
                Accessed: 15/01/2024
                URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
            */
            waitForTime.countDown()
        }
        waitForTime.await()

        // Saves the current user profile to the device
        saveData(gameManager.userProfile)
    }

    // Gets the accurate time from the API
    private fun getAccurateTime(location: Array<Double>)
    {
        val client = OkHttpClient()
        val url = "https://timeapi.io/api/Time/current/coordinate?latitude=" + location[0] + "&longitude=" + location[1]
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                e.printStackTrace()
                /*
                    This line is used to wait for the response from the api to be finished
                    before moving forward
                    Author: foxwendy
                    Accessed: 15/01/2024
                    URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
                */
                // Triggers waitForTime to continue
                waitForTime.countDown()
            }

            override fun onResponse(call: Call, response: Response)
            {
                response.use {
                    try
                    {
                        val tempCurrentTime = JSONObject(response.body!!.string())
                        currentTime.hours = tempCurrentTime.getInt("hour")
                        currentTime.minutes = tempCurrentTime.getInt("minute")
                        currentTime.date = tempCurrentTime.getInt("day")
                        currentTime.month = tempCurrentTime.getInt("month")
                        currentTime.year = tempCurrentTime.getInt("year")
                    }
                    catch (e: JSONException)
                    {
                        e.printStackTrace()
                    }
                    /*
                        This line is used to wait for the response from the api to be finished
                        Author: foxwendy
                        Accessed: 15/01/2024
                        URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
                    */
                    // Triggers waitForTime to continue
                    waitForTime.countDown()
                }
            }
        })
    }

    // Saves the user profile to the device
    private fun saveData(userProfile: UserProfile)
    {
        /*
            This script is inspired by the java code for saving an object to local storage
            Author: naikus
            Accessed: 15/01/2024
            URL: https://stackoverflow.com/questions/3625837/android-what-is-wrong-with-openfileoutput
        */
        try
        {
            val fos = openFileOutput("UserProfile", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)

            oos.writeObject(userProfile)
            oos.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        // End of cited code
    }
}