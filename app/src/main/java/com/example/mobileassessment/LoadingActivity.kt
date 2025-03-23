package com.example.mobileassessment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class LoadingActivity : AppCompatActivity(), LocationListener
{

    // The user profile instance loaded from a file
    private var profile: UserProfile? = null

    // The reference to the loading bar for the application
    private lateinit var loadingBar: ProgressBar

    // The latitude and longitude of the user
    private var locationPos = arrayOf(0.0,0.0)

    // The location manager used to get the location of the user
    private lateinit var locationManager: LocationManager

    // The current accurate time from the API
    private val currentTime = Date(0,0,0,0,0)

    /*
        This line is used to wait for the response from the api to be finished
        before moving forward
        Author: foxwendy
        Accessed: 15/01/2024
        URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
     */
    // The countdown used to track when the time API has returned a response
    private var finish = CountDownLatch(1)

    // The connectivity manager service
    private lateinit var networkService: ConnectivityManager

    /*
        This line is inspired by the examples from the Android Developer documentation
        Author: Android Developer (individual author name unknown)
        Accessed: 20/01/2024
        URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
    */
    // The executor service used to run the API call on a separate thread
    private val executorService: ExecutorService = Executors.newFixedThreadPool(1)
    // End of inspired line

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        networkService = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // Checks if the user is connected to a network
        if(networkService.activeNetwork != null)
        {
            val ability = networkService.getNetworkCapabilities(networkService.activeNetwork)

            // Checks if the user can connect to the internet
            if(!ability!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            {
                // Sends the user to an activity asking them to connect to the internet
                val intent = Intent(this, ConnectToInternet::class.java)
                startActivity(intent)
            }
            else
            {
                // Sets up the loading bar
                loadingBar = findViewById(R.id.loadingBar)
                loadingBar.max = 4

                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

                /*
                    The locationManager.isProviderEnabled code was taken from here
                    Author: GAMA
                    Accessed: 21/01/2024
                    URL: https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
                 */
                // Checks if the application has permission to get the location and if the location provider is enabled
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    // End of cited code
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,this)

                    // Gets the saved user profile
                    val savedProfile = readData()

                    if(savedProfile != null)
                    {
                        profile = savedProfile

                        /*
                            The use of the executor service here is inspired by the examples from the Android Developer documentation
                            Author: Android Developer (individual author name unknown)
                            Accessed: 20/01/2024
                            URL: https://developer.android.com/develop/background-work/background-tasks/asynchronous/java-threads
                        */
                        // Gets the accurate time on a separate thread
                        executorService.execute{
                            getAccurateTime(locationPos)
                        }
                        // End of inspired code

                        // Waits for the time to be returned from the API
                        finish.await()

                        // Checks current time against the saved time
                        // Returns -1 if the current time is after the saved time
                        // Returns 0 if the times are on the same day
                        //Returns 1 if the saved time is after the current time
                        val comparedDates = currentTime.compareTo(profile!!.lastLogin)

                        if(comparedDates == 0)
                        {
                            // Calculates the amount of idle damage that needs to happen from the time passed
                            val differenceHours = (currentTime.hours - profile!!.lastLogin.hours) * 60 * 60
                            val differenceMin = (currentTime.minutes - profile!!.lastLogin.minutes) * 60
                            val differenceSec = currentTime.seconds - profile!!.lastLogin.seconds
                            profile!!.damageFromTime = differenceHours + differenceMin + differenceSec * profile!!.tickDamage
                        }
                        else if(comparedDates < 0)
                        {
                            // Calculates the amount of idle damage that needs to happen from the time passed
                            // Idle damage has a maximum of a tick every minute of a day
                            profile!!.damageFromTime = 24*60 * profile!!.tickDamage
                        }
                        else
                        {
                            profile!!.damageFromTime = 0
                        }
                    }
                    else // If there is no saved user profile a new one is made and used
                    {
                        profile = UserProfile()

                        profile!!.damageFromTime = 0
                        profile!!.lastLogin = currentTime
                    }

                    // Calculates the amount of monsters defeated by the overtime damage
                    profile!!.calculateDungeonDamage()

                    // Moves the application to the main activity
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("UserProfile", profile)
                    startActivity(intent)
                }
                else // Runs if the app doesn't have access to the location service
                {
                    // Changes the activity to an activity which requests the location permissions
                    val intent = Intent(this, LocationRequest::class.java)
                    startActivity(intent)
                }
            }
        }
        else //Runs if the device is not connected to a network or cannot connect to the internet
        {
            // Changes the activity with one that allows the user to retry the connection to the internet
            val intent = Intent(this, ConnectToInternet::class.java)
            startActivity(intent)
        }
    }

    // Runs when the location is updated from the location service
    override fun onLocationChanged(location: Location)
    {
        // Saves the latitude and longitude of the device
        locationPos[0] = location.latitude
        locationPos[1] = location.longitude

        // Removes the listener from the location manager as the location is only needed once
        locationManager.removeUpdates(this)
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
                // Triggers finish to continue
                finish.countDown()
            }

            override fun onResponse(call: Call, response: Response)
            {
                response.use {
                    try
                    {
                        // Parses and saves the API response as a Date
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
                        before moving forward
                        Author: foxwendy
                        Accessed: 15/01/2024
                        URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
                     */
                    // Triggers finish to continue
                    finish.countDown()
                }
            }
        })
    }

    // Gets a saved profile from the internal files
    // Returns null if no save file was found
    private fun readData(): UserProfile?
    {
        /*
            This script is reverse engineered from the java code for saving an object to local storage
            Author: naikus
            Accessed: 15/01/2024
            URL: https://stackoverflow.com/questions/3625837/android-what-is-wrong-with-openfileoutput
        */
        try
        {
            val fos = openFileInput("UserProfile")
            val ois = ObjectInputStream(fos)
            val savedProfile = ois.readObject() as UserProfile
            ois.close()
            return savedProfile
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return null
        }
        // End of cited code
    }
}