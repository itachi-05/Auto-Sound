package com.alpharays.autosound.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.alpharays.autosound.data.api.LocationService
import com.alpharays.autosound.data.api.PlaceData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SoundWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val location: String? = inputData.getString("location")
            val dataList: ArrayList<String> = ArrayList()

            location?.let {
                LocationService.locationInstance.getPlaces(location, "40")
                    .enqueue(object : Callback<PlaceData> {
                        override fun onResponse(
                            call: Call<PlaceData>,
                            response: Response<PlaceData>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let { placeData ->
                                    val resultsResponse = placeData.results
                                    for (res in resultsResponse) {
                                        dataList.add(res.vicinity)
                                        dataList.add(res.name)
                                    }
                                    val outputData = Data.Builder()
                                        .putStringArray("result", dataList.toTypedArray())
                                        .build()

                                    // Pass the result to the UI
                                    Result.success(outputData)
                                }
                                Log.i("Response_1", response.body().toString())
                            } else {
                                Log.i("Response_0", response.message())
                            }
                        }

                        override fun onFailure(call: Call<PlaceData>, t: Throwable) {
                            Log.i("ON_Failure", "Failed")
                        }
                    })
            }

            // Return success to indicate the work is still in progress
            return Result.success()
        } catch (e: Exception) {
            // Handle any errors or failures
            return Result.failure()
        }
    }
}

