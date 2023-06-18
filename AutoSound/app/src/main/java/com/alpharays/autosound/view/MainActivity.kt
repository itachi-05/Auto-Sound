package com.alpharays.autosound.view

import android.Manifest
import android.R
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
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
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar
import com.google.android.libraries.places.api.net.PlacesClient
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import androidx.work.Constraints.Builder;
import androidx.work.Data
import androidx.work.WorkerParameters
import com.alpharays.autosound.util.AlarmManagerHandler
import com.alpharays.autosound.util.LocationWorker
import com.alpharays.autosound.util.SoundWorker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private val triggerViewModel: TriggerViewModel by lazy { ViewModelProvider(this)[TriggerViewModel::class.java] }
    private val locationViewModel: LocationViewModel by lazy { ViewModelProvider(this)[LocationViewModel::class.java] }
    private lateinit var triggerCardsAdapter: TriggerCardsAdapter
    private var isGpsAsked = false
    private var alarmManagerHandler: AlarmManagerHandler? = null
    private val audioManager: AudioManager by lazy {
        this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val REQUEST_CODE_LOCATION_PERMISSION = 123
    private val triggersList: MutableList<Trigger> = ArrayList()
    private lateinit var notificationManager: NotificationManager
    private lateinit var placesClient: PlacesClient
    private var isLocationPermissionGranted = false
    private val locationLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private val autoSoundSharedPref: SharedPreferences by lazy {
        getSharedPreferences(
            "autoSoundModeSharedPref#0",
            MODE_PRIVATE
        )
    }


    //private var settings : SharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
    //val editor : SharedPreferences.Editor = settings.edit()
    //var settings : SharedPreferences = getPreferences(MODE_PRIVATE)
    private val FIRST_RUN_KEY = "firstRun"
    private val PREFERENCE_NAME = "MyAppPreferences"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //var settings : SharedPreferences = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
//        editor.apply{
//            putBoolean("firstTime",true)
//        }.apply()
        checkFirstRun(this)

        if (alarmManagerHandler == null) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManagerHandler = AlarmManagerHandler(this, alarmManager)
        }

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
                    Toast.makeText(this, trigger.id.toString(), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AddTriggerActivity::class.java)
                    intent.putExtra("Data", trigger)
                    startActivity(intent)
                }
                triggerCardsAdapter.setOnActionDeleteListener { trigger ->
                    //triggerViewModel.deleteTrigger(trigger)
                    confirmDelete(trigger)
                }
                triggerCardsAdapter.notifyDataSetChanged()
            }
        }

        // checking places api:
        val loc = "28.638384994491613,77.0638923540995"
        val loc_new = "28.638180242340077,77.04984428672424"

//        val isModeOn = autoSoundSharedPref.getBoolean("sharedPrefAutoSound_1", false)
//        binding.autoSoundMode.isChecked = isModeOn
//
//        binding.autoSoundMode.setOnCheckedChangeListener { _, isChecked ->
//            // Perform actions based on the checked state
//            if (isChecked) {
//                // Auto sound mode is enabled
//                autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", true).apply()
//                Toast.makeText(this, "On", Toast.LENGTH_SHORT).show()
//            } else {
//                // Auto sound mode is disabled
//                autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", false).apply()
//                Toast.makeText(this, "Off", Toast.LENGTH_SHORT).show()
//            }
//        }

        if (isLocationEnabled()) {
            isGpsAsked = true
            val locationTag = "location_worker"
            val soundTag = "update_location"

            val isModeOn = autoSoundSharedPref.getBoolean("sharedPrefAutoSound_1", false)
            binding.autoSoundMode.isChecked = isModeOn
            if (isModeOn) {
                // Location Worker already running
//                LocationWorker.deployWork(this)
            } else {
                WorkManager.getInstance(this).cancelUniqueWork(locationTag)
                WorkManager.getInstance(this).cancelUniqueWork(soundTag)
            }

            binding.autoSoundMode.setOnCheckedChangeListener { _, isChecked ->
                // Perform actions based on the checked state
                if (isChecked) {
                    // Auto sound mode is enabled
                    autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", true).apply()
                    LocationWorker.deployWork(this)
                    Toast.makeText(this, "AutoSound Mode On", Toast.LENGTH_LONG).show()
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
                    // Auto sound mode is disabled
                    autoSoundSharedPref.edit().putBoolean("sharedPrefAutoSound_1", false).apply()
                    WorkManager.getInstance(this).cancelUniqueWork(locationTag)
                    WorkManager.getInstance(this).cancelUniqueWork(soundTag)
                    Toast.makeText(this, "AutoSound Mode Off", Toast.LENGTH_LONG).show()
                }
            }
        }

//        locationViewModel.fetchLocation(loc_new).observe(this) { list ->
//            list?.let {
//                // search this data and give it to job scheduler
//                for (i in it.indices) {
//                    Log.i("searching_Place $i", it[i])
//                }
//            }
//        }

    }


    private fun confirmDelete(trigger: Trigger) {
        val deleteAlert = AlertDialog.Builder(this)
        deleteAlert.setTitle("Delete")
        deleteAlert.setMessage("Are you sure?")
        deleteAlert.setPositiveButton("YES") { _, _ ->
            triggerViewModel.deleteTrigger(trigger)
            alarmManagerHandler?.cancelAlarm(trigger.id.toInt())
        }
        deleteAlert.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        deleteAlert.show()
    }

    private fun checkFirstRun(context: Context) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        val isFirstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true)

        if (isFirstRun) {
            // Perform the task for the first time running the app
            // For example, show an onboarding screen or display a welcome message
            //performFirstTimeTask()
            TapTargetView.showFor(
                this, TapTarget.forView(binding.fab, "Create New Trigger")
                    .outerCircleAlpha(0.6f)
                    .titleTextSize(20)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(40)
            )
            // Set the flag to indicate that the app has been run before
            val editor = sharedPreferences.edit()
            editor.putBoolean(FIRST_RUN_KEY, false)
            editor.apply()
        } //else {
        // The app has been run before, perform regular operations here
        //performRegularTask()
        //}
    }

    override fun onResume() {
        super.onResume()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            showDialog()
        } else {
            if (!isLocationPermissionGranted) {
                requestPermissions()
            } else if (!isLocationEnabled()) displayLocationEnableDialog()
            else if (isLocationPermissionGranted && isLocationEnabled() && !isGpsAsked) {
                LocationWorker.deployWork(this)
            }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Handle location updates if needed
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }

        override fun onProviderEnabled(provider: String) {
            // Location provider (GPS, network) is enabled
            // Handle the enabled state if needed
            locationLiveData.postValue(true)

        }

        override fun onProviderDisabled(provider: String) {
            // Location provider (GPS, network) is disabled
            // Handle the disabled state if needed
            locationLiveData.postValue(false)
        }
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
            onResume()
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