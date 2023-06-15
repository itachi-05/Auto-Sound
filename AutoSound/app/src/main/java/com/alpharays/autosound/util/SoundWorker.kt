package com.alpharays.autosound.util

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.work.Data
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alpharays.autosound.data.api.LocationService
import com.alpharays.autosound.data.api.PlaceData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class SoundWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val audioManager: AudioManager by lazy {
        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override suspend fun doWork(): Result {
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val location = "$latitude,$longitude"
        val dataList: ArrayList<String> = ArrayList()

        Log.i("location_Worker", location)

        LocationService.locationInstance.getPlaces(location, "10")
            .enqueue(object : Callback<PlaceData> {
                override fun onResponse(
                    call: Call<PlaceData>,
                    response: Response<PlaceData>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { placeData ->

                            val keywords =
                                listOf("hospital", "academy", "school", "coaching", "temple")
                            var targetFound = false

                            for (res in placeData.results) {
                                val vicinity = res.vicinity.lowercase(Locale.getDefault())
                                val name = res.name.lowercase(Locale.getDefault())

                                dataList.add(res.vicinity)
                                dataList.add(res.name)

                                val containsKeyword = keywords.any { keyword ->
                                    (vicinity.contains(keyword) || name.contains(keyword)) && !(vicinity.contains(
                                        "near $keyword"
                                    ) || name.contains("near $keyword"))
                                }

                                if (containsKeyword) {
                                    targetFound = true
                                    Log.i("data#Loc#1", res.vicinity)
                                    Log.i("data#Loc#1", res.name)
                                    break
                                }
                            }

//                            for (data in dataList) {
//                                Log.i("data#Loc", data)
//                            }

                            if (targetFound) {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    0,
                                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                )
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_NOTIFICATION,
                                    0,
                                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                )
                            } else {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    100,
                                    AudioManager.FLAG_VIBRATE
                                )
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_NOTIFICATION,
                                    100,
                                    AudioManager.FLAG_VIBRATE
                                )
                            }


//                            outputData = Data.Builder()
//                                .putStringArray("result", dataList.toTypedArray())
//                                .build()

//                            Log.i("Response_1", response.body().toString())
                        }
                    } else {
                        Log.i("Response_0", response.message())
                    }
                }

                override fun onFailure(call: Call<PlaceData>, t: Throwable) {
                    Log.i("ON_Failure", "Failed")
                }
            })

        val currentTimeMillis = System.currentTimeMillis()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime =
            Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).format(formatter)
        Log.i("success_work", "DO_WORK $formattedDateTime")
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "update_location"

        /**
         * Work request for sync every 15 minutes
         */
        fun deployWork(context: Context, inputData: Data) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
            val request = PeriodicWorkRequestBuilder<SoundWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }
    }
}