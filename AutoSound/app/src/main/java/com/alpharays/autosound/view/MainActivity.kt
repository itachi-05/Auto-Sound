package com.alpharays.autosound.view

import android.Manifest
import android.R
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alpharays.autosound.adapter.TriggerCardsAdapter
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.databinding.ActivityMainBinding
import com.alpharays.autosound.util.TrackingUtility
import com.alpharays.autosound.viewmodel.LocationViewModel
import com.alpharays.autosound.viewmodel.TriggerViewModel
import com.google.android.libraries.places.api.net.PlacesClient
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import androidx.work.Constraints.Builder;
import com.alpharays.autosound.util.SoundWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private val triggerViewModel: TriggerViewModel by lazy { ViewModelProvider(this)[TriggerViewModel::class.java] }
    private val locationViewModel: LocationViewModel by lazy { ViewModelProvider(this)[LocationViewModel::class.java] }
    private lateinit var triggerCardsAdapter: TriggerCardsAdapter
    private val REQUEST_CODE_LOCATION_PERMISSION = 123
    private val triggersList: MutableList<Trigger> = ArrayList()
    private lateinit var notificationManager: NotificationManager
    private lateinit var placesClient: PlacesClient
    private var isLocationPermissionGranted = false
    private val autoSoundSharedPref: SharedPreferences by lazy {
        getSharedPreferences(
            "autoSoundModeSharedPref#0",
            MODE_PRIVATE
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.triggersRv.layoutManager = LinearLayoutManager(this)

        triggerCardsAdapter = TriggerCardsAdapter(triggersList)

        binding.triggersRv.adapter = triggerCardsAdapter

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddTriggerActivity::class.java))
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        triggerViewModel.allTriggers.observe(this) {
            it?.let { allTriggers ->
                triggersList.clear()
                triggersList.addAll(allTriggers)
                triggerCardsAdapter.setOnActionEditListener { trigger ->
                    val intent = Intent(this, AddTriggerActivity::class.java)
                    intent.putExtra("Data", trigger)
                    startActivity(intent)
                }
                triggerCardsAdapter.setOnActionDeleteListener { trigger ->
                    confirmDelete(trigger)
                }
                triggerCardsAdapter.notifyDataSetChanged()
            }
        }

        // Initialize the SDK
//        val MAPS_API_KEY = "AIzaSyDYS2nGfVF2EeKD2W0OQipondywFYZxaGc"
//        Places.initialize(applicationContext, MAPS_API_KEY)

        // Create a new PlacesClient instance
//        placesClient = Places.createClient(this)

        // checking places api:
        val loc = "28.638384994491613,77.0638923540995"
        val loc_new = "28.638180242340077,77.04984428672424"

        val isModeOn = autoSoundSharedPref.getBoolean("sharedPrefAutoSound_1", false)
        binding.autoSoundMode.isChecked = isModeOn

        binding.autoSoundMode.setOnCheckedChangeListener { _, isChecked ->
            // Perform actions based on the checked state
            if (isChecked) {
                // Auto sound mode is enabled
                autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", true).apply()
                Toast.makeText(this, "On", Toast.LENGTH_SHORT).show()
            } else {
                // Auto sound mode is disabled
                autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", false).apply()
                Toast.makeText(this, "Off", Toast.LENGTH_SHORT).show()
            }
        }

        workManager()


        locationViewModel.fetchLocation(loc_new).observe(this) { list ->
            list?.let {
                // search this data and give it to job scheduler
                for (i in it.indices) {
                    Log.i("searching_Place $i", it[i])
                }
            }
        }

    }

    private fun workManager() {
        // Set up the constraints for the work
        // Set up the constraints for the work
        val constraints: Constraints = Builder().build()

        // Create the periodic work request to run every 15 minutes
        val workRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(SoundWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(workRequest)
    }


    private fun confirmDelete(trigger: Trigger) {
        val deleteAlert = AlertDialog.Builder(this)
        deleteAlert.setTitle("Delete")
        deleteAlert.setMessage("Are you sure?")
        deleteAlert.setPositiveButton("YES") { _, _ ->
            triggerViewModel.deleteTrigger(trigger)
        }
        deleteAlert.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        deleteAlert.show()
    }

    override fun onResume() {
        super.onResume()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            showDialog()
        } else {
            if (!isLocationPermissionGranted) {
                requestPermissions()
            } else if (!isLocationEnabled()) {
                displayLocationEnableDialog()
            }
        }
//        if (isLocationEnabled()) {
//            // Everything is set up
//            // Use fields to define the data types to return.
//            val placeFields: List<Place.Field> = listOf(Place.Field.NAME)
//
//            // Use the builder to create a FindCurrentPlaceRequest.
//            val request: FindCurrentPlaceRequest =
//                FindCurrentPlaceRequest.newInstance(placeFields)
//
//            // Call findCurrentPlace and handle the response (first check that the user has granted permission).
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) ==
//                PackageManager.PERMISSION_GRANTED
//            ) {
//
//                val placeResponse = placesClient.findCurrentPlace(request)
//                placeResponse.addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val response = task.result
//                        for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods
//                            ?: emptyList()) {
//                            Log.i(
//                                "TAG_1",
//                                "Place '${placeLikelihood.place.name}' has likelihood: ${placeLikelihood.likelihood}"
//                            )
//                        }
//                    } else {
//                        val exception = task.exception
//                        if (exception is ApiException) {
//                            Log.e("TAG", "Place not found: ${exception.statusCode}")
//                        }
//                    }
//                }
//            } else {
//                // A local method to request required permissions;
//                // See https://developer.android.com/training/permissions/requesting
//                requestPermissions()
//            }
//        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun displayLocationEnableDialog() {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Location required")
        alertDialogBuilder.setMessage("Please enable location to use this app.")
        alertDialogBuilder.setIcon(R.drawable.ic_dialog_alert)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(
            "Enable"
        ) { dialog, which ->
            val locationSettingsIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(locationSettingsIntent)
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("You need to accept Do Not Disturb permission to use this app.")
        builder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            showDialog()
        }
        builder.setCancelable(false)
        builder.show()
    }

    // new permission handling:
    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            isLocationPermissionGranted = true
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
//        locationPermissionStatus.postValue(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}