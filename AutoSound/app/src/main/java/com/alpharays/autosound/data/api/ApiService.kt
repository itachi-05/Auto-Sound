package com.alpharays.autosound.data.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
const val API_KEY = "AIzaSyDYS2nGfVF2EeKD2W0OQipondywFYZxaGc"
interface LocationInterface {
    @GET("nearbysearch/json")
    fun getPlaces(
        @Query("location") location: String,
        @Query("radius") radius: String,
        @Query("key") apiKey: String = API_KEY
    ): Call<PlaceData>
}

object LocationService {
    val locationInstance: LocationInterface

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        locationInstance = retrofit.create(LocationInterface::class.java)
    }
}