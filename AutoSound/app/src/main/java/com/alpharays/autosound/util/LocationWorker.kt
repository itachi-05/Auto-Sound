package com.alpharays.autosound.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // Get the current location
            val location = getCurrentLocation()

            // Create a data object to pass the location
            val data = Data.Builder()
                .putDouble("latitude", location?.latitude ?: 0.0)
                .putDouble("longitude", location?.longitude ?: 0.0)
                .build()

            // Pass the location data to another worker
            val workerRequest = OneTimeWorkRequestBuilder<SoundWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workerRequest)

            val currentTimeMillis = System.currentTimeMillis()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDateTime =
                Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).format(formatter)
            Log.i("success_location", "DO_WORK $location")

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        return suspendCoroutine { continuation ->
            val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
            val granted = ContextCompat.checkSelfPermission(applicationContext, locationPermission) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } else {
                continuation.resume(null)
            }
        }
    }

    private suspend fun getLastKnownLocation(): Location? {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)

        return suspendCoroutine { continuation ->
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } else {
                continuation.resume(null)
            }
        }
    }

    companion object {
        private const val WORK_NAME = "location_worker"

        /**
         * Work request for sync every 15 minutes
         */
        fun deployWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }
    }
}