package com.example.mobileassessment

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.io.Serializable
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.M)
class Dungeon: Serializable
{
    // Reference to all the monsters in the dungeon
    var monsters: ArrayList<Monster> = ArrayList(0)

    // The most recently created monster
    private var newMonster:Monster = Monster("", "", "",0)

    // The reference to the active monster of the dungeon
    var currentMonster = Monster("","", "",0)

    // The length of the dungeon
    private var length = ""

    // The difficulty of the dungeon
    private var difficulty = ""

    // The total amount of monsters in the dungeon
    private var amountOfMon = 0

    constructor(difficulty: String, length: String) {
        this.length = length
        this.difficulty = difficulty
    }

    // Gets and returns the next monster of the dungeon. Returns null if the dungeon is completed
    fun getEncounter(userProfile: UserProfile) : Monster?
    {
        // Removes the defeated monster
        monsters.remove(currentMonster)

        // Gives the user currency based on the difficulty of the
        when(difficulty)
        {
            "easy" -> {
                userProfile.currency1 += 1
            }

            "normal" -> {
                userProfile.currency1 += 2
            }

            "hard" -> {
                userProfile.currency1 += 3
            }
        }

        //Checks if all the monsters have been defeated
        if(monsters.size != 0)
        {
            // Returns the next monster if there are monsters left
            currentMonster = monsters[0]
            return currentMonster
        }
        else
        {
            // Returns null if the dungeon is complete
            return null
        }
    }

    // Generates all the monsters needed for the dungeon
    @RequiresApi(Build.VERSION_CODES.M)
    fun generateMonsters()
    {
        // Changes the amount of monsters based on the length of the dungeon
        when (length)
        {
            "short" ->
            {
                amountOfMon = Random.nextInt(3, 5)
            }

            "medium" ->
            {
                amountOfMon = Random.nextInt(10, 15)
            }

            "long" ->
            {
                amountOfMon = Random.nextInt(20, 30)
            }
        }

        var x = 0

        // Calls the Digimon API and generates the monsters
        while (x < amountOfMon)
        {
            monsters.add(getNewMonster(difficulty))
            x++
        }
        // Sets the first monster of the dungeon
        currentMonster = monsters[0]
    }

    // Gets a new monster using the Digimon API
    @RequiresApi(Build.VERSION_CODES.M)
    fun getNewMonster(difficulty: String): Monster
    {
        var url = ""

        // Changes the Digimon list with difficulty by calling different sections of the API
        when(difficulty)
        {
            "easy" ->
            {
                url = "https://digimon-api.vercel.app/api/digimon/level/rookie"
            }

            "normal" ->
            {
                url = "https://digimon-api.vercel.app/api/digimon/level/Champion"
            }

            "hard" ->
            {
                url = "https://digimon-api.vercel.app/api/digimon/level/Ultimate"
            }
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        /*
            This line is used to wait for the response from the api to be finished
            before moving forward
            Author: foxwendy
            Accessed: 15/01/2024
            URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
         */
        val count = CountDownLatch(1)
        // End of cited code

        // Gets the monsters information from the Digimon API
        client.newCall(request).enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                val tempMonster =  Monster("", "","", 2)
                newMonster.name = tempMonster.name
                newMonster.imageURL = tempMonster.imageURL
                newMonster.errorCode = tempMonster.errorCode
                e.printStackTrace()
                /*
                    This line is used to wait for the response from the api to be finished
                    before moving forward
                    Author: foxwendy
                    Accessed: 15/01/2024
                    URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
                */
                count.countDown()
                // End of cited code
            }

            override fun onResponse(call: Call, response: Response)
            {
                response.use {
                    val tempMonster = processMonsterJson(response.body!!.string())
                    newMonster.name = tempMonster.name
                    newMonster.imageURL = tempMonster.imageURL
                    newMonster.level = tempMonster.level
                    newMonster.errorCode = tempMonster.errorCode
                    /*
                        This line is used to wait for the response from the api to be finished
                        before moving forward
                        Author: foxwendy
                        Accessed: 15/01/2024
                        URL: https://stackoverflow.com/questions/34597220/how-to-wait-for-the-result-on-a-okhttp-call-to-use-it-on-a-test
                     */
                    count.countDown()
                    // End of cited code
                }
            }
        })
        // Waits for the AOI request to finish
        count.await()
        currentMonster = Monster(newMonster.name, newMonster.imageURL, newMonster.level, newMonster.errorCode)
        return currentMonster
    }

    // Processes the JSON data from the API
    fun processMonsterJson(data: String): Monster
    {
        return try
        {
            val jsonData = JSONArray(data)
            val test = jsonData.getJSONObject(Random.nextInt(0,jsonData.length()))
            Monster( test.getString("name"),test.getString("img"),test.getString("level"), 0)
        }
        catch (e: JSONException)
        {
            e.printStackTrace()
            Monster("", "","", 1)
        }
    }

}