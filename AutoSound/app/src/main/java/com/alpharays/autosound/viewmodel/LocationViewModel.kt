package com.alpharays.autosound.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpharays.autosound.data.api.LocationService
import com.alpharays.autosound.data.api.PlaceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationViewModel : ViewModel() {
    fun fetchLocation(location: String): LiveData<ArrayList<String>> {
        val fetchingLocation: MutableLiveData<ArrayList<String>> = MutableLiveData()
        viewModelScope.launch(Dispatchers.IO) {
            LocationService.locationInstance.getPlaces(location, "40")
                .enqueue(object : Callback<PlaceData> {
                    override fun onResponse(call: Call<PlaceData>, response: Response<PlaceData>) {
                        val dataList : ArrayList<String> = ArrayList()
                        if (response.isSuccessful) {
                            response.body()?.let { placeData ->
                                val resultsResponse = placeData.results
                                for(res in resultsResponse){
                                    dataList.add(res.vicinity)
                                    dataList.add(res.name)
                                }
                                fetchingLocation.postValue(dataList)
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
        return fetchingLocation
    }
}