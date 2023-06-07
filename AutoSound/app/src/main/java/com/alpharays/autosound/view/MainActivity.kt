package com.alpharays.autosound.view

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpharays.autosound.adapter.TriggerCardsAdapter
import com.alpharays.autosound.data.trigger.Trigger
import com.alpharays.autosound.databinding.ActivityMainBinding
import com.alpharays.autosound.util.Constants
import com.alpharays.autosound.util.TrackingUtility
import com.alpharays.autosound.viewmodel.TriggerViewModel
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.EasyPermissions
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val triggerViewModel: TriggerViewModel by lazy { ViewModelProvider(this)[TriggerViewModel::class.java] }
    private lateinit var triggerCardsAdapter: TriggerCardsAdapter
    private val triggersList: MutableList<Trigger> = ArrayList()
    private var isExecutedOnResume = false
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        if(!notificationManager.isNotificationPolicyAccessGranted){
//            requestPermissions()
//        }

//        if(!isExecutedOnResume){
//            isExecutedOnResume = true
//            requestPermissions()
//        }

//        val date = getCurrentLocalDate()
//        val trigger1 =
//            Trigger(false, "1", "10:00", date, Constants.RingerMode.Normal.name, 30, 50, 70)
//        val trigger2 =
//            Trigger(false, "2", "11:00", date, Constants.RingerMode.Normal.name, 40, 60, 80)
//        val trigger3 =
//            Trigger(false, "3", "12:00", date, Constants.RingerMode.Normal.name, 50, 70, 90)
//        val trigger4 =
//            Trigger(false, "4", "13:00", date, Constants.RingerMode.Normal.name, 60, 80, 40)
//        val trigger5 =
//            Trigger(false, "5", "14:00", date, Constants.RingerMode.Normal.name, 70, 90, 50)

//        triggersList.add(trigger1)
//        triggersList.add(trigger2)
//        triggersList.add(trigger3)
//        triggersList.add(trigger4)
//        triggersList.add(trigger5)
//        triggersList.add(trigger1)
//        triggersList.add(trigger2)
//        triggersList.add(trigger3)
//        triggersList.add(trigger4)
//        triggersList.add(trigger5)
//        triggersList.add(trigger1)
//        triggersList.add(trigger2)
//        triggersList.add(trigger3)
//        triggersList.add(trigger4)
//        triggersList.add(trigger5)

        binding.triggersRv.layoutManager = LinearLayoutManager(this)

        triggerCardsAdapter = TriggerCardsAdapter(triggersList)

        binding.triggersRv.adapter = triggerCardsAdapter

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddTriggerActivity::class.java))
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // adding to room db
//        triggerViewModel.createTrigger(trigger1)
//        triggerViewModel.createTrigger(trigger2)
//        triggerViewModel.createTrigger(trigger3)
//        triggerViewModel.createTrigger(trigger4)
//        triggerViewModel.createTrigger(trigger5)

        for (i in 0..20) {
            val isAlarmSet = PendingIntent.getBroadcast(
                this,
                i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) != null

            if (isAlarmSet) {
                // Alarm is already set
                Log.i("test#0$i", "YES")
            } else {
                // Alarm is not set
                Log.i("test#0$i", "NO")
            }
        }


        // testing room db
        triggerViewModel.allTriggers.observe(this) {
            it?.let { allTriggers ->
                triggersList.clear()
                triggersList.addAll(allTriggers)
//                for (trigger in allTriggers) {
//                    Log.i(
//                        "triggerData",
//                        "${trigger.id} ${trigger.isRepeat} ${trigger.triggerTime} ${trigger.ringerMode}"
//                    )
//                }
                triggerCardsAdapter.setOnActionEditListener { trigger ->
                    val intent = Intent(this, AddTriggerActivity::class.java)
                    intent.putExtra("Data", trigger)
                    startActivity(intent)
                }
                triggerCardsAdapter.setOnActionDeleteListener {
                    //val builder = AlertDialog.Builder(this)
                    //builder.setMessage("Are you sure?")
                    //builder.setPositiveButton("YES",DialogInterface.OnClickListener())

                }
                triggerCardsAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            showDialog()
        }
    }

    private fun getCurrentLocalDate(): Date {
        val currentDate = LocalDate.now()
        return Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    // new permission handling:
    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("You need to accept Do Not Disturb permission to use this app.")
        builder.setPositiveButton("OK") { dialog, which ->
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // handle what happens when user presses cancel, you may want to call showDialog() again to persist the dialog
            showDialog()
        }
        builder.setCancelable(false) // This prevents the user from dismissing the dialog by pressing back button
        builder.show()
    }

}

// the following block was created to suppress an error
private fun Intent.putExtra(s: String, it: Trigger) {

}
