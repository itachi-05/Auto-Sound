package com.alpharays.autosound.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alpharays.autosound.data.trigger.Trigger
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmManagerHandler(
    private val context: Context,
    private val alarmManager: AlarmManager
) {

    fun setAlarm(requestCode: Int, trigger: Trigger) {
        Log.d("NEW_TAG", "setAlarm: requestCode=$requestCode, trigger=$trigger")

        val intent = Intent(context, AutoSoundTriggerReceiver::class.java)
        intent.putExtra(Constants.RINGER_MODE_BUNDLE_NAME, trigger.ringerMode)
        val arrayList = arrayListOf(trigger.ringerVolume, trigger.mediaVolume, trigger.alarmVolume)
        intent.putExtra(Constants.VOLUMES_BUNDLE_NAME, arrayList.toIntArray())
        val timeInfo = trigger.triggerTime
        intent.putExtra("timeInfo", timeInfo)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val timeInMillis = trigger.triggerTime.toLong()

        if (trigger.isRepeat) {
            Log.d("TAG2", "setAlarm: Setting repeating alarm")
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        } else {
            Log.d("TAG2", "setAlarm: Setting single alarm")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AutoSoundTriggerReceiver::class.java)
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } else {
            Log.i("alarmDoesNotExist", "NA")
        }
    }

}